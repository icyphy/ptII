/* Select, CGC domain: CGCSelect.java file generated from /users/ptolemy/src/domains/cgc/stars/CGCSelect.pl by ptlang
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
//// CGCSelect
/**
This star requires a BDF scheduler!

If the value on the 'control' line is nonzero, trueInput
is copied to the output; otherwise, falseInput is.

 @Author J. T. Buck
 @Version $Id$, based on version 1.4 of /users/ptolemy/src/domains/cgc/stars/CGCSelect.pl, from Ptolemy Classic 
 @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCSelect extends ClassicCGCActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCSelect(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        trueInput = new ClassicPort(this, "trueInput", true, false);
        falseInput = new ClassicPort(this, "falseInput", true, false);
        control = new ClassicPort(this, "control", true, false);
        control.setTypeEquals(BaseType.INT);
        output = new ClassicPort(this, "output", false, true);

/* 
*/
    }
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /**
     * trueInput of type anytype.
     */
    public ClassicPort trueInput;

    /**
     * falseInput of type anytype.
     */
    public ClassicPort falseInput;

    /**
     * control of type int.
     */
    public ClassicPort control;

    /**
     * output of type anytype.
     */
    public ClassicPort output;

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////

    /**
     */
    public int  myExecTime() {
        
return 0;
     }

    /**
     */
    public void  generateInitializeCode() throws IllegalActionException {
        
// all connected buffers must be size 1, for now.
		// need to check this.
		// FIXME: we can permit output to be > 1 by generating
		// a copy of the (shared) input buffer to the output.
		if (notone(output) || notone(trueInput) ||
		    notone(falseInput) || notone(control))
			throw new IllegalActionException(this,
		"Non-unity buffers connected to a Select not yet supported");
		else {
			// make all the buffers overlap.
			output.embed(trueInput,0);
			output.embed(falseInput,0);
			trueInput.setRelation(DF_true,&control);
			falseInput.setRelation(DF_false,&control);
		}
     }

    /**
     */
    protected int notone (CGCPortHole& port) {
        
return (port.numInitDelays() > 1 ||
				port.far()->numXfer() > 1);
    }

}
