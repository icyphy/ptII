/* Spread, CGC domain: CGCSpread.java file generated from /users/ptolemy/src/domains/cgc/stars/CGCSpread.pl by ptlang
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
//// CGCSpread
/**
Takes one input and produces multiple outputs.
<p>
This star does not generate any code. In multiprocessor code generation
domain, this star will be automatically attached to a porthole whose
outputs are passed to more than one destination (one ordinary block and
one Send star, more than one Send stars, and so on.)

 @Author S. Ha
 @Version $Id$, based on version 1.9 of /users/ptolemy/src/domains/cgc/stars/CGCSpread.pl, from Ptolemy Classic
 @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCSpread extends ClassicCGCActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCSpread(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input = new ClassicPort(this, "input", true, false);
        output = new ClassicPort(this, "output", false, true);
        output.setMultiport(true);

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

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////

    /**
     */
    public void  generateInitializeCode() throws IllegalActionException {

MPHIter iter(output);
                CGCPortHole* p;
                int loc = 0;
                while ((p = (CGCPortHole*) iter++) != 0) {
                        input.embed(*p, loc);
                        loc += p->numXfer();
                }
     }

    /**
     */
    public void  generateFireCode() {

     }

    /**
     */
    protected int amISpreadCollect () {

return -1;
    }

}
