/* Switch, CGC domain: CGCSwitch.java file generated from /users/ptolemy/src/domains/cgc/stars/CGCSwitch.pl by ptlang
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
//// CGCSwitch
/**
This star requires a BDF scheduler.
Switches input events to one of two outputs, depending on
the value of the control input.  If control is true, the
value is written to trueOutput; otherwise it is written to
falseOutput.

 @Author J. T. Buck
 @Version $Id$, based on version 1.4 of /users/ptolemy/src/domains/cgc/stars/CGCSwitch.pl, from Ptolemy Classic
 @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCSwitch extends ClassicCGCActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCSwitch(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input = new ClassicPort(this, "input", true, false);
        control = new ClassicPort(this, "control", true, false);
        control.setTypeEquals(BaseType.INT);
        trueOutput = new ClassicPort(this, "trueOutput", false, true);
        falseOutput = new ClassicPort(this, "falseOutput", false, true);

/*
trueOutput.setRelation(DF_true, &control);
                falseOutput.setRelation(DF_false, &control);
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
     * trueOutput of type anytype.
     */
    public ClassicPort trueOutput;

    /**
     * falseOutput of type anytype.
     */
    public ClassicPort falseOutput;

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
                // FIXME: we can permit input to be > 1 by generating
                // a copy of the input buffer to the (shared) output.
                if (notone(input) || notone(trueOutput) ||
                    notone(falseOutput) || notone(control))
                        throw new IllegalActionException(this,
                "Non-unity buffers connected to a switch not yet supported");
                else {
                        // make all the buffers overlap.
                        input.embed(trueOutput,0);
                        input.embed(falseOutput,0);
                        trueOutput.setRelation(DF_true, &control);
                        falseOutput.setRelation(DF_false, &control);
                }
     }

    /**
     */
    protected int notone (CGCPortHole& port) {

return (port.numInitDelays() > 1 ||
                                port.far()->numXfer() > 1);
    }

}
