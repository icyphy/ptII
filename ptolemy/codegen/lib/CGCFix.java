/* Fix, CGC domain: CGCFix.java file generated from /users/ptolemy/src/domains/cgc/stars/CGCFix.pl by ptlang
*/
/*
Copyright (c) 1990-1997 The Regents of the University of California.
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
//// CGCFix
/**
Based star for the fixed-point stars in the CGC domain.

 @Author Juergen Weiss
 @Version $Id$, based on version 1.8 of /users/ptolemy/src/domains/cgc/stars/CGCFix.pl, from Ptolemy Classic
 @Since Ptolemy II 4.1 and at least Ptolemy Classic 0.7.1, possibly earlier.
*/
public class CGCFix extends ClassicCGCActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CGCFix(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // If non-zero, e.g. YES or true, then after a simulation has
        // finished,\nthe star will report the number of overflow
        // errors if any occurred\nduring the simulation. IntState

        ReportOverflow = new Parameter(this, "ReportOverflow");
        ReportOverflow.setExpression("NO");

        // counter for overflow errors IntState
        ov_cnt = new Parameter(this, "ov_cnt");
        ov_cnt.setExpression("0");

        // counter for overflow check operations IntState
        ck_cnt = new Parameter(this, "ck_cnt");
        ck_cnt.setExpression("0");

    }
    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /**
     * If non-zero, e.g. YES or true, then after a simulation has
     * finished, the star will report the number of overflow errors if any
     * occurred during the simulation. parameter with initial value "NO".
     */

     public Parameter ReportOverflow;

    /**
     *  counter for overflow errors parameter with initial value "0".
     */
     public Parameter ov_cnt;

    /**
     *  counter for overflow check operations parameter with initial
     *  value "0".
     */
     public Parameter ck_cnt;

    ///////////////////////////////////////////////////////////////////
    ////                     public methods                        ////

    /**
     */
    public void  wrapup() {

        if (((IntToken)((ReportOverflow).getToken())).intValue() == 1)
            // FIXME ReportOverflow should be a Boolean
            {
                StringBuffer s = new StringBuffer(this.getFullName());
                addCode(report_overflow(s.toString()));
            }
     }

    /**
     */
    public void  generatePreinitializeCode() {
        // No need to add addInclude("<stdio.h>") here,
        // addFixedPointSupport includes CGCrtlib.c,
        // which includes stdio.h
        addFixedPointSupport();
    }

    /**
     */
    public void  generateInitializeCode() throws IllegalActionException {
        // do nothing
    }

    /**
     */
    protected void clearOverflow () {
        if (((IntToken)((ReportOverflow).getToken())).intValue() == 1)
            // FIXME ReportOverflow should be a Boolean
            {
                addCode("\tfix_overflow = 0; \n");
            }
    }

    /**
     */
    protected void checkOverflow () {

        if (((IntToken)((ReportOverflow).getToken())).intValue() == 1)
            // FIXME ReportOverflow should be a Boolean
            {
                addCode("\tif ($ref(ck_cnt)++, fix_overflow)\n");
                addCode("\t\t$ref(ov_cnt)++; \n");
            }
    }

    ///////////////////////////////////////////////////////////////////
    ////                     Codeblocks                     ////

    public String report_overflow (String      starName) {
        return
            "        if ($ref(ov_cnt)) {\n"
            + "                double percentage = (100.0*$ref(ov_cnt)) / ($ref(ck_cnt) ? $ref(ck_cnt):1.0);\n"
            + "                fprintf(stderr, \"star " + starName + ": experienced overflow in %d out of %d fixed-point calculations checked (%.1f%%)\\n\",\n"
            + "                        $ref(ov_cnt), $ref(ck_cnt), percentage);\n"
            + "        }\n";
    }
}
