/* BlackHole, CGC domain: CGCBlackHole.java file generated from /users/ptolemy/src/domains/cgc/stars/CGCBlackHole.pl by ptlang
 */
/*
  Copyright (c) 1990-2005 The Regents of the University of California.
  All rights reserved.
  See the file $PTOLEMY/copyright for copyright notice,
  limitation of liability, and disclaimer of warranty provisions.
*/
package ptolemy.codegen.lib;

import ptolemy.codegen.kernel.ClassicCGCActor;
import ptolemy.codegen.kernel.ClassicPort;
import ptolemy.codegen.kernel.CodeGeneratingActor;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// CGCBlackHole
/**
   Discards all inputs.
   <p>
   A BlackHole accepts input Particles, but doesn't do anything with
   them.  It is typically used to discard unwanted outputs from other blocks.

   @Author J. T. Buck
   @Version $Id$, based on version 1.13 of /users/ptolemy/src/domains/cgc/stars/CGCBlackHole.pl, from Ptolemy Classic
   @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCBlackHole extends ClassicCGCActor implements CodeGeneratingActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCBlackHole(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input = new ClassicPort(this, "input", true, false);
        input.setMultiport(true);

        /*     //# line 25 "/users/ptolemy/src/domains/cgc/stars/CGCBlackHole.pl"
               noInternalState();
        */
    }
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /**
     * input of type anytype.
     */
    public ClassicPort input;

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////

    /**
     */
    public int  myExecTime() {
        //# line 31 "/users/ptolemy/src/domains/cgc/stars/CGCBlackHole.pl"
        return 0;
    }

    /**
     */
    public void  generateFireCode() {
        //# line 28 "/users/ptolemy/src/domains/cgc/stars/CGCBlackHole.pl"
        addCode("/* This actor generates only this comment. */");
    }
}
