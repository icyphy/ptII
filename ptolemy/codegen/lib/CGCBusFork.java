/* BusFork, CGC domain: CGCBusFork.java file generated from /users/ptolemy/src/domains/cgc/stars/CGCBusFork.pl by ptlang
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
//// CGCBusFork
/**
Copy particles from an input bus to each output bus.
<p>
To keep the implementation simple, this star does not attempt the
fancy tricks performed by the regular fork star to avoid runtime overhead.
The data values are simply copied at runtime.

 @Author E. A. Lee
 @Version $Id$, based on version 1.6 of /users/ptolemy/src/domains/cgc/stars/CGCBusFork.pl, from Ptolemy Classic
 @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCBusFork extends ClassicCGCActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCBusFork(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input = new ClassicPort(this, "input", true, false);
        input.setMultiport(true);
        outputA = new ClassicPort(this, "outputA", false, true);
        outputA.setMultiport(true);
        outputB = new ClassicPort(this, "outputB", false, true);
        outputB.setMultiport(true);

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
     * outputA of type anytype.
     */
    public ClassicPort outputA;

    /**
     * outputB of type anytype.
     */
    public ClassicPort outputB;

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////

    /**
     */
    public int  myExecTime() {

// Alert: rough estimate
          return input.numberPorts();
     }

    /**
     */
    public void  begin() {

if (input.numberPorts() != outputA.numberPorts() ||
              input.numberPorts() != outputB.numberPorts()) {
            throw new IllegalActionException(this, "Bus widths don't match");
            return;
          }
     }

    /**
     */
    public void  generateFireCode() {

MPHIter nextin(input), nextoutA(outputA), nextoutB(outputB);
          PortHole *in, *outA, *outB;
          int port = 0;
          while (((in = nextin++) != 0) &&
                 ((outA = nextoutA++) != 0) &&
                 ((outB = nextoutB++) != 0)) {
            port++;
{ StringBuffer _str_ = new StringBuffer(); _str_.append(
"        $ref(outputA#" + port + ") = $ref(outputB#" + port + ") = $ref(input#" + port + ");\n"

);          addCode(_str_);  }
          }
     }
}
