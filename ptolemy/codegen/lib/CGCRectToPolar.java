/* RectToPolar, CGC domain: CGCRectToPolar.java file generated from /users/ptolemy/src/domains/cgc/stars/CGCRectToPolar.pl by ptlang
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
//// CGCRectToPolar
/**
   Convert two numbers to magnitude and phase.
   The phase output is in the range -PI to PI.

   @Author S. Ha
   @Version $Id$, based on version 1.7 of /users/ptolemy/src/domains/cgc/stars/CGCRectToPolar.pl, from Ptolemy Classic 
   @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCRectToPolar extends ClassicCGCActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCRectToPolar(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        x = new ClassicPort(this, "x", true, false);
        x.setTypeEquals(BaseType.DOUBLE);
        y = new ClassicPort(this, "y", true, false);
        y.setTypeEquals(BaseType.DOUBLE);
        magnitude = new ClassicPort(this, "magnitude", false, true);
        magnitude.setTypeEquals(BaseType.DOUBLE);
        phase = new ClassicPort(this, "phase", false, true);
        phase.setTypeEquals(BaseType.DOUBLE);

        /*     
               noInternalState();
        */
    }
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /**
     * x of type double.
     */
    public ClassicPort x;

    /**
     * y of type double.
     */
    public ClassicPort y;

    /**
     * magnitude of type double.
     */
    public ClassicPort magnitude;

    /**
     * phase of type double.
     */
    public ClassicPort phase;

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////

    /**
     */
    public int  myExecTime() {
        
        return 200;	/* based on CG96 stars */
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
    "	double xpart, ypart, m;\n"
    + "	xpart = $ref(x);\n"
    + "	ypart = $ref(y);\n"
    + "	m = sqrt(xpart*xpart + ypart*ypart);\n"
    + "	$ref(magnitude) = m;\n"
    + "	if (m == 0) $ref(phase) = 0;\n"
    + "	else $ref(phase) = atan2(ypart,xpart);\n";
}
