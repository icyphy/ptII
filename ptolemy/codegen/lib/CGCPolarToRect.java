/* PolarToRect, CGC domain: CGCPolarToRect.java file generated from /users/ptolemy/src/domains/cgc/stars/CGCPolarToRect.pl by ptlang
*/
/*
Copyright (c) 1990-2005 The Regents of the University of California.
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
//// CGCPolarToRect
/**
Convert magnitude and phase to rectangular form.

 @Author S. Ha
 @Version $Id$, based on version 1.7 of /users/ptolemy/src/domains/cgc/stars/CGCPolarToRect.pl, from Ptolemy Classic
 @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCPolarToRect extends ClassicCGCActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCPolarToRect(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        magnitude = new ClassicPort(this, "magnitude", true, false);
        magnitude.setTypeEquals(BaseType.DOUBLE);
        phase = new ClassicPort(this, "phase", true, false);
        phase.setTypeEquals(BaseType.DOUBLE);
        x = new ClassicPort(this, "x", false, true);
        x.setTypeEquals(BaseType.DOUBLE);
        y = new ClassicPort(this, "y", false, true);
        y.setTypeEquals(BaseType.DOUBLE);

/*
noInternalState();
*/
    }
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /**
     * magnitude of type double.
     */
    public ClassicPort magnitude;

    /**
     * phase of type double.
     */
    public ClassicPort phase;

    /**
     * x of type double.
     */
    public ClassicPort x;

    /**
     * y of type double.
     */
    public ClassicPort y;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     */
    public int  myExecTime() {

return 50;
     }

    /**
     */
    public void  generatePreinitializeCode() {

addInclude("<math.h>");
     }

    /**
     */
    public void  generateFireCode() {

addCode(body);
     }
    ///////////////////////////////////////////////////////////////////
    ////                     Codeblocks                     ////

    public String body =
        "        double m,p;\n"
        + "        m = $ref(magnitude);\n"
        + "        p = $ref(phase);\n"
        + "        $ref(x) = m * cos(p);\n"
        + "        $ref(y) = m * sin(p);\n";
}
