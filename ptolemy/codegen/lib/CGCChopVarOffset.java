/* ChopVarOffset, CGC domain: CGCChopVarOffset.java file generated from /users/ptolemy/src/domains/cgc/stars/CGCChopVarOffset.pl by ptlang
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
//// CGCChopVarOffset
/**
This star has the same functionality as the
Chop star except now the offset parameter is
determined at run time through a control input.

 @Author S. Ha
 @Version $Id$, based on version 1.6 of /users/ptolemy/src/domains/cgc/stars/CGCChopVarOffset.pl, from Ptolemy Classic 
 @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCChopVarOffset extends CGCChop {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCChopVarOffset(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        offsetCntrl = new ClassicPort(this, "offsetCntrl", true, false);
        offsetCntrl.setTypeEquals(BaseType.INT);

/*     
noInternalState();
		offset.setAttributes(A_NONCONSTANT|A_NONSETTABLE);
*/
    }
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /**
     * offsetCntrl of type int.
     */
    public ClassicPort offsetCntrl;

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////

    /**
     */
    public int  myExecTime() {
        
return super.myExecTime() + 8;
     }

    /**
     */
    public void  generateFireCode() {
        
addCode(decl); 			// look at CGCChop star
		addCode(init); 
		addCode(range); 
		if (input.resolvedType() == COMPLEX) 
		  addCode(complexOut); 		// look at CGCChop star
		else
		  addCode(nonComplexOut); 	// look at CGCChop star
     }
    ///////////////////////////////////////////////////////////////////
    ////                     Codeblocks                     ////

    public String init = 
        "	$ref(offset) = $ref(offsetCntrl);\n";

    public String range = 
        "        int hiLim = $val(nwrite) - $ref(offset) - 1;\n"
        + "        if (hiLim >= $val(nwrite)) hiLim = $val(nwrite) - 1;\n"
        + "        else if ($val(use_past_inputs)) hiLim = $val(nwrite) - 1;\n"
        + "\n"
        + "        int loLim = $val(nwrite) - $val(nread) - $ref(offset);\n"
        + "        if (loLim < 0) loLim = 0;\n"
        + "\n"
        + "        int inidx = $val(nread) - $val(nwrite) + $ref(offset);\n"
        + "        if (inidx < 0) inidx = 0;\n";
}
