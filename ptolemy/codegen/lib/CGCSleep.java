/* Sleep, CGC domain: CGCSleep.java file generated from /users/ptolemy/src/domains/cgc/stars/CGCSleep.pl by ptlang
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
//// CGCSleep
/**
Suspend execution for an interval (in milliseconds).
The input is passed to the output when the process resumes.

 @Author E. A. Lee
 @Version $Id$, based on version 1.6 of /users/ptolemy/src/domains/cgc/stars/CGCSleep.pl, from Ptolemy Classic 
 @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCSleep extends ClassicCGCActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCSleep(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input = new ClassicPort(this, "input", true, false);
        input.setTypeEquals(BaseType.DOUBLE);
        output = new ClassicPort(this, "output", false, true);
        output.setTypeEquals(BaseType.DOUBLE);

        // Time in milliseconds to sleep. IntState
        interval = new Parameter(this, "interval");
        interval.setExpression("10");

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

    /**
     *  Time in milliseconds to sleep. parameter with initial value "10".
     */
     public Parameter interval;

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////

    /**
     */
    public void  generatePreinitializeCode() {
        
addInclude("<sys/types.h>");
		addInclude("<sys/time.h>");
     }

    /**
     */
    public void  generateFireCode() {
        
addCode(std); 
     }
    ///////////////////////////////////////////////////////////////////
    ////                     Codeblocks                     ////

    public String std = 
        "	    {\n"
        + "	    static struct timeval delay;\n"
        + "	    delay.tv_sec = $val(interval)/1000;\n"
        + "	    delay.tv_usec = ($val(interval)%1000)*1000;\n"
        + "	    (void) select(0, (fd_set *) 0, (fd_set *) 0,\n"
        + "        	    (fd_set *) 0, &delay);\n"
        + "	    }\n"
        + "	    $ref(output) = $ref(input);\n";
}
