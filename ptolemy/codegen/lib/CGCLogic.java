/* Logic, CGC domain: CGCLogic.java file generated from /users/ptolemy/src/domains/cgc/stars/CGCLogic.pl by ptlang
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
//// CGCLogic
/**
This star applies a logical operation to any number of inputs.
The inputs are integers interpreted as Boolean values,
where zero means false and a nonzero value means true.
The logical operations supported are {NOT AND NAND OR NOR XOR XNOR}.
<p>
The NOT operation requires that there be only one input.
The XOR operation with multiple inputs tests for an odd number
of true values among the inputs.
The other operations are self-explanatory.

 @Author Brian L. Evans and Edward A. Lee
 @Version $Id$, based on version 1.5 of /users/ptolemy/src/domains/cgc/stars/CGCLogic.pl, from Ptolemy Classic 
 @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCLogic extends ClassicCGCActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCLogic(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input = new ClassicPort(this, "input", true, false);
        input.setMultiport(true);
        input.setTypeEquals(BaseType.INT);
        output = new ClassicPort(this, "output", false, true);
        output.setTypeEquals(BaseType.INT);

        // The test logic: one of NOT AND NAND OR NOR XOR or XNOR. StringState
        logic = new Parameter(this, "logic");
        logic.setExpression("AND");

/*     
noInternalState();
*/
    }
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /**
Input logic values.
     */
    public ClassicPort input;

    /**
Result of the logic test, with false equal to zero and true equal to a
non-zero integer (not necessarily 1).
     */
    public ClassicPort output;

    /**
     *  The test logic: one of NOT AND NAND OR NOR XOR or XNOR. parameter with initial value "AND".
     */
     public Parameter logic;

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////

    /**
     */
    public void  generateInitializeCode() throws IllegalActionException {
        
String cn = logic;
		if ( cn.equalsIgnoreCase("NOT")) {
		    test = NOTID;
		    if (input.numberPorts() > 1)
			throw new IllegalActionException(this,
			    "NOT operation can only have one input");
		}
		else if ( cn.equalsIgnoreCase("AND") ) test = ANDID;
		else if ( cn.equalsIgnoreCase("NAND") ) test = NANDID;
		else if ( cn.equalsIgnoreCase("OR") ) test = ORID;
		else if ( cn.equalsIgnoreCase("NOR") ) test = NORID;
		else if ( cn.equalsIgnoreCase("XOR") ) test = XORID;
		else if ( cn.equalsIgnoreCase("XNOR") ) test = XNORID;
		else throw new IllegalActionException(this, "Unrecognized test.");
     }

    /**
     */
    public void  generateFireCode() {
        
// The inverter (not) star is the simplest case
		if ( test == NOTID ) {
			addCode("\t$ref(output) = ! $ref(input#1); \n");
			return;
		}

		// Declare and initialize local variables i and result
		if ( test == ANDID || test == NANDID ) {
			addCode("\tint result = 1; \n");
		}
		else {
			addCode("\tint result = 0; \n");
		}

		// Generate the code that walks through the input values
		int i = 1;
		switch( test ) {
		  case ANDID:
		  case NANDID:
			for (i = 1; i <= input.numberPorts(); i++ ) {
			    addCode(logicAndOp(i)); 
			}
			break;
		  case ORID:
		  case NORID:
			for (i = 1; i <= input.numberPorts(); i++ ) {
			    addCode(logicOrOp(i)); 
			}
			break;
		  case XORID:
		  case XNORID:
			for (i = 1; i <= input.numberPorts(); i++ ) {
			    addCode(logicXorOp(i)); 
			    break;
			}
		}

		// Compute final result 
		StringBuffer finalCode = new StringBuffer();
		if (test == NANDID || test == NORID || test == XNORID ) {
			finalCode.append("\tresult = ! result;\n");
		}
		finalCode.append("\t$ref(output) = result;\n");
		addCode(finalCode); 
     }
    ///////////////////////////////////////////////////////////////////
    ////                     Codeblocks                     ////

    public String logicAndOp (int i) {
        return
        "	result = result && $ref(input#" + i + ");\n";
    }

    public String logicOrOp (int i) {
        return
        "	result = result || $ref(input#" + i + ");\n";
    }

    public String logicXorOp (int i) {
        return
        "	if ( $ref(input#" + i + ") ) result = ! result;\n";
    }
}
