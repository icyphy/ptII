/* Collect, CGC domain: CGCCollect.java file generated from /users/ptolemy/src/domains/cgc/stars/CGCCollect.pl by ptlang
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
//// CGCCollect
/**
Takes multiple inputs and produces one output.
<p>
This star does not generate codes. In multiprocessor code generation domain,
it will be automatically attached to a porthole if the sources of the porthole
is more than one. Its role is just opposite to that of Spread star.

 @Author S. Ha
 @Version $Id$, based on version 1.10 of /users/ptolemy/src/domains/cgc/stars/CGCCollect.pl, from Ptolemy Classic
 @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCCollect extends ClassicCGCActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCCollect(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input = new ClassicPort(this, "input", true, false);
        input.setMultiport(true);
        output = new ClassicPort(this, "output", false, true);

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

MPHIter iter(input);
                CGCPortHole* p;
                int loc = 0;
                while ((p = (CGCPortHole*) iter++) != 0) {
                        output.embed(*p, loc);
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

return 1;
    }

}
