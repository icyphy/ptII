/* MpyInt, CGC domain: CGCMpyInt.java file generated from /users/ptolemy/src/domains/cgc/stars/CGCMpyInt.pl by ptlang
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
//// CGCMpyInt
/**
   Output the complex product of the inputs.

   @Author Brian L. Evans and Jose Luis Pino
   @Version $Id$, based on version 1.1 of /users/ptolemy/src/domains/cgc/stars/CGCMpyInt.pl, from Ptolemy Classic 
   @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCMpyInt extends ClassicCGCActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCMpyInt(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input = new ClassicPort(this, "input", true, false);
        input.setMultiport(true);
        input.setTypeEquals(BaseType.INT);
        output = new ClassicPort(this, "output", false, true);
        output.setTypeEquals(BaseType.INT);

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

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////

    /**
     */
    public int  myExecTime() {
        
        return input.numberPorts() + 1;
    }

    /**
     */
    public void  generateFireCode() {
        
        addCode(startOp); 
	for (int i = 2; i <= input.numberPorts(); i++) 
	    addCode(doOp(i));  
	addCode(saveResult); 
    }
    ///////////////////////////////////////////////////////////////////
    ////                     Codeblocks                     ////

    public String startOp = 
    "	int sum = $ref(input#1);\n";

    public String doOp (int i) {
        return
            "	sum *= $ref(input#" + i + ");\n";
    }

    public String saveResult = 
    "	$ref(output) = sum;\n";
}
