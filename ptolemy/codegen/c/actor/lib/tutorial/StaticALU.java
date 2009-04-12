/* A code generation helper class for actor.lib.logic.StaticALU

 @Copyright (c) 2005-2009 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the
 above copyright notice and the following two paragraphs appear in all
 copies of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN \"AS IS\" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY

 */
package ptolemy.codegen.c.actor.lib.tutorial;

import java.util.ArrayList;

import ptolemy.codegen.c.kernel.CCodeGeneratorHelper;
import ptolemy.kernel.util.IllegalActionException;

/**
 A code generation helper class for ptolemy.actor.lib.logic.StaticALU.

 @author Man-Kit Leung
 @version $Id$
 @since Ptolemy II 6.0
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
 */
public class StaticALU extends CCodeGeneratorHelper {

    /**
     * Construct the Comparator helper.
     * @param actor The associated actor.
     */
    public StaticALU(ptolemy.actor.lib.tutorial.StaticALU actor) {
        super(actor);
    }

    /**
     * Generate fire code.
     * The method reads in <code>fireBlock</code> from Accumulator.c,
     * replaces macros with their values and returns the processed code
     * block.
     * @return The generated code.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    protected String _generateFireCode() throws IllegalActionException {

        StringBuffer code = new StringBuffer();

        code.append(super._generateFireCode());

        /* Add code here */
        ptolemy.actor.lib.tutorial.StaticALU actor = (ptolemy.actor.lib.tutorial.StaticALU) getComponent();

        int opcode = Integer.parseInt(actor.operation.getExpression()
                .substring(0, 1));

        if (opcode == 0) {
            code.append(_generateBlockCode("nopBlock"));

        } else {
            ArrayList args = new ArrayList();

            switch (opcode) {
            case 1:
                args.add("+");
                break;
            case 2:
                args.add("-");
                break;
            case 3:
                args.add("*");
                break;
            case 4:
                args.add("/");
                break;
            }
            code.append(_generateBlockCode("operationBlock", args));
        }

        return processCode(code.toString());
    }
}
