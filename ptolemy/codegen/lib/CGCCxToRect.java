/* CxToRect, CGC domain: CGCCxToRect.java file generated from /users/ptolemy/src/domains/cgc/stars/CGCCxToRect.pl by ptlang
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
//// CGCCxToRect
/**
Convert complex data to real and imaginary parts.

 @Author S. Ha
 @Version $Id$, based on version 1.6 of /users/ptolemy/src/domains/cgc/stars/CGCCxToRect.pl, from Ptolemy Classic
 @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCCxToRect extends ClassicCGCActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCCxToRect(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input = new ClassicPort(this, "input", true, false);
        input.setTypeEquals(BaseType.COMPLEX);
        real = new ClassicPort(this, "real", false, true);
        real.setTypeEquals(BaseType.DOUBLE);
        imag = new ClassicPort(this, "imag", false, true);
        imag.setTypeEquals(BaseType.DOUBLE);

/*
noInternalState();
*/
    }
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /**
     * input of type complex.
     */
    public ClassicPort input;

    /**
     * real of type double.
     */
    public ClassicPort real;

    /**
     * imag of type double.
     */
    public ClassicPort imag;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     */
    public int  myExecTime() {

return 2;
     }

    /**
     */
    public void  generateFireCode() {

addCode(body);
     }
    ///////////////////////////////////////////////////////////////////
    ////                     Codeblocks                     ////

    public String body =
        "        $ref(real) = $ref(input).real;\n"
        + "        $ref(imag) = $ref(input).imag;\n";
}
