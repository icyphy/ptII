/* Ramp, CGC domain: Ramp.java file generated from /users/ptolemy/src/domains/cgc/stars/Ramp.pl by ptlang
 */
/*
  Copyright (c) 1990-1996 The Regents of the University of California.
  All rights reserved.
  See the file $PTOLEMY/copyright for copyright notice,
  limitation of liability, and disclaimer of warranty provisions.
*/
package ptolemy.codegen.c.actor.lib;

import ptolemy.codegen.kernel.ClassicCGCActor;
import ptolemy.codegen.kernel.ClassicPort;
import ptolemy.codegen.kernel.CodeGeneratingActor;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// Ramp
/**
   Generates a ramp signal, starting at "value" (default 0)
   with step size "step" (default 1).

   @Author E. A. Lee
   @Version $Id$, based on version 1.9 of /users/ptolemy/src/domains/cgc/stars/Ramp.pl, from Ptolemy Classic 
   @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class Ramp extends ClassicCGCActor implements CodeGeneratingActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public Ramp(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        output = new ClassicPort(this, "output", false, true);
        output.setTypeEquals(BaseType.DOUBLE);

        // Increment from one sample to the next. FloatState
        step = new Parameter(this, "step");
        step.setExpression("1.0");

        // Initial (or latest) value output by Ramp. FloatState
        value = new Parameter(this, "value");
        value.setExpression("0.0");

        /* 
         */
    }
    
    public Ramp(ptolemy.actor.lib.Ramp actor)
            throws IllegalActionException, NameDuplicationException {
        //super(container, name);
        
        //Question: being a helper class, do we need it to be an actor?
        
        //Question: being a helper class, calling numberPorts() won't give
        // useful information. Two ways to solve it: 1) in the generated
        // code, replace numberPorts() with getWidth(), then we don't need
        // to create port for this helper class, a reference to the
        // corresponding port in the "real" actor can be used. 2) still keep
        // numberPorts(), but need to direct this method to getWidth()
        // of the corresponding port in the "real" actor.
        
        output = new ClassicPort(this, "output", false, true);
        output.setTypeEquals(actor.output.getType());

        // Increment from one sample to the next. FloatState
        step = new Parameter(this, "step");
        step.setToken(actor.step.getToken());

        // Initial (or latest) value output by Ramp. FloatState
        value = new Parameter(this, "value");
        value.setToken(actor.init.getToken());

/* 
 */
}
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /**
     * output of type double.
     */
    public ClassicPort output;

    /**
     *  Increment from one sample to the next. parameter with initial value "1.0".
     */
    public Parameter step;

    /**
     *  Initial (or latest) value output by Ramp. parameter with initial value "0.0".
     */
    public Parameter value;

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////

    /**
     */
    public int  myExecTime() {
        
        return 2;
    }

    /**
     */
    public void  generateFireCode() {
        
        addCode(std); 
    }
    ///////////////////////////////////////////////////////////////////
    ////                     Codeblocks                     ////

    public String std = 
    "	$ref(output) = $ref(value);\n"
    + "	$ref(value) += $val(step);\n";
}
