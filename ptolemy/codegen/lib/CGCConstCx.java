/* ConstCx, CGC domain: CGCConstCx.java file generated from /users/ptolemy/src/domains/cgc/stars/CGCConstCx.pl by ptlang
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
//// CGCConstCx
/**
Output the complex constant.

 @Author Jose Luis Pino
 @Version $Id$, based on version 1.1 of /users/ptolemy/src/domains/cgc/stars/CGCConstCx.pl, from Ptolemy Classic
 @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCConstCx extends ClassicCGCActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCConstCx(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        output = new ClassicPort(this, "output", false, true);
        output.setTypeEquals(BaseType.COMPLEX);

        // Real part of DC value. FloatState
        real = new Parameter(this, "real");
        real.setExpression("0.0");

        // Imaginary part of DC value. FloatState
        imag = new Parameter(this, "imag");
        imag.setExpression("0.0");

/*
noInternalState();
*/
    }
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /**
     * output of type complex.
     */
    public ClassicPort output;

    /**
     *  Real part of DC value. parameter with initial value "0.0".
     */
     public Parameter real;

    /**
     *  Imaginary part of DC value. parameter with initial value "0.0".
     */
     public Parameter imag;

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////

    /**
     */
    public int  myExecTime() {

return 0;
     }

    /**
     */
    public void  generateFireCode() {

{ StringBuffer _str_ = new StringBuffer(); _str_.append(
"	$ref(output).real = $val(real);\n"
"	$ref(output).imag = $val(imag);"

); 	 addCode(_str_);  }

     }
}
