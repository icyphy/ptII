/* Sub, CGC domain: CGCSub.java file generated from /users/ptolemy/src/domains/cgc/stars/CGCSub.pl by ptlang
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
//// CGCSub
/**
Output the "pos" input minus all "neg" inputs.

 @Author E. A. Lee
 @Version $Id$, based on version 1.11 of /users/ptolemy/src/domains/cgc/stars/CGCSub.pl, from Ptolemy Classic 
 @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCSub extends ClassicCGCActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCSub(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        pos = new ClassicPort(this, "pos", true, false);
        pos.setTypeEquals(BaseType.DOUBLE);
        neg = new ClassicPort(this, "neg", true, false);
        neg.setMultiport(true);
        neg.setTypeEquals(BaseType.DOUBLE);
        output = new ClassicPort(this, "output", false, true);
        output.setTypeEquals(BaseType.DOUBLE);

        // index IntState
        index = new Parameter(this, "index");
        index.setExpression("1");

/*     
noInternalState();
*/
    }
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /**
     * pos of type double.
     */
    public ClassicPort pos;

    /**
     * neg of type double.
     */
    public ClassicPort neg;

    /**
     * output of type double.
     */
    public ClassicPort output;

    /**
     *  index parameter with initial value "1".
     */
     public Parameter index;

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////

    /**
     */
    public int  myExecTime() {
        
return 1 + neg.numberPorts();
     }

    /**
     */
    public void  generateFireCode() {
        
addCode(init);  
	   for (int i = 1; i <= neg.numberPorts(); i++) {
		index = i;
		addCode("\t$ref(output) -= $ref(neg#index); \n");
	   }
     }
    ///////////////////////////////////////////////////////////////////
    ////                     Codeblocks                     ////

    public String init = 
        "	$ref(output) = $ref(pos);\n";
}
