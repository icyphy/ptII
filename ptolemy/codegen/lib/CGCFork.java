/* Fork, CGC domain: CGCFork.java file generated from /users/ptolemy/src/domains/cgc/stars/CGCFork.pl by ptlang
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
//// CGCFork
/**
Copy input to all outputs
<p>
Each input is copied to every output.  This is done by the way the buffers
are laid out; no code is required.

 @Author E. A. Lee
 @Version $Id$, based on version 1.12 of /users/ptolemy/src/domains/cgc/stars/CGCFork.pl, from Ptolemy Classic
 @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCFork extends ClassicCGCActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCFork(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input = new ClassicPort(this, "input", true, false);
        output = new ClassicPort(this, "output", false, true);
        output.setMultiport(true);

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

forkInit(input,output);
     }
}
