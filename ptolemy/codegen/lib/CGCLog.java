/* Log, CGC domain: CGCLog.java file generated from /users/ptolemy/src/domains/cgc/stars/CGCLog.pl by ptlang
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
//// CGCLog
/**
Outputs natural log of input.
<p>
Outputs natural log of input.
If the input is zero or negative, the constant -100.0 is produced.
<a name="logarithm"></a>

 @Author E. A. Lee
 @Version $Id$, based on version 1.7 of /users/ptolemy/src/domains/cgc/stars/CGCLog.pl, from Ptolemy Classic
 @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCLog extends ClassicCGCActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCLog(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input = new ClassicPort(this, "input", true, false);
        input.setTypeEquals(BaseType.DOUBLE);
        output = new ClassicPort(this, "output", false, true);
        output.setTypeEquals(BaseType.DOUBLE);

/*
noInternalState();
*/
    }
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /**
     * input of type double.
     */
    public ClassicPort input;

    /**
     * output of type double.
     */
    public ClassicPort output;

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////

    /**
     */
    public int  myExecTime() {

return 33;  /* based on CG96Log */
     }

    /**
     */
    public void  generatePreinitializeCode() {

addInclude("<math.h>");
     }

    /**
     */
    public void  generateFireCode() {

addCode(ln);
     }
    ///////////////////////////////////////////////////////////////////
    ////                     Codeblocks                     ////

    public String ln =
        "        if ($ref(input) <= 0) $ref(output) = -100.0;\n"
        + "        else $ref(output) = log($ref(input));\n";
}
