/* A code generation helper class for actor.lib.BooleanSelect

 @Copyright (c) 2006-2009 The Regents of the University of California.
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
package ptolemy.codegen.c.actor.lib;

import java.util.ArrayList;

import ptolemy.codegen.c.kernel.CCodeGeneratorHelper;
import ptolemy.kernel.util.IllegalActionException;

/**
 A code generation helper class for ptolemy.actor.lib.BooleanSelect.

 @author Man-Kit Leung
 @version $Id$
 @since Ptolemy II 6.0
 @Pt.ProposedRating Green (mankit)
 @Pt.AcceptedRating Green (mankit)
 */
public class BooleanSelect extends CCodeGeneratorHelper {

    /**
     * Construct a BooleanSelect helper.
     * @param actor The associated actor.
     */
    public BooleanSelect(ptolemy.actor.lib.BooleanSelect actor) {
        super(actor);
    }

    /**
     * Return the code generated for sending the true input data.
     * Iterate through the connections to the true port
     * and append the "trueBlock" code block from the template.
     * @return The generated code.
     * @exception IllegalActionException Thrown if an error occurs
     * when getting the width for the port or the code block.
     */
    public String sendTrueInputs() throws IllegalActionException {
        ptolemy.actor.lib.BooleanSelect actor = (ptolemy.actor.lib.BooleanSelect) getComponent();

        int width = Math.min(actor.output.getWidth(),
                actor.trueInput.getWidth());

        StringBuffer code = new StringBuffer();
        ArrayList args = new ArrayList();
        args.add(0);

        for (int i = 0; i < width; i++) {
            args.set(0, i);
            code.append(_generateBlockCode("trueBlock", args));
        }
        return processCode(code.toString());
    }

    /**
     * Return the code generated for sending the false input data.
     * Iterate through the connections to the true port
     * and append the "falseBlock" code block from the template.
     * @return The generated code.
     * @exception IllegalActionException Thrown if an error occurs
     * when getting the width for the port or the code block.
     */
    public String sendFalseInputs() throws IllegalActionException {
        ptolemy.actor.lib.BooleanSelect actor = (ptolemy.actor.lib.BooleanSelect) getComponent();

        int width = Math.min(actor.output.getWidth(),
                actor.trueInput.getWidth());

        StringBuffer code = new StringBuffer();
        ArrayList args = new ArrayList();
        args.add(0);

        for (int i = 0; i < width; i++) {
            args.set(0, i);
            code.append(_generateBlockCode("falseBlock", args));
        }
        return processCode(code.toString());
    }
}
