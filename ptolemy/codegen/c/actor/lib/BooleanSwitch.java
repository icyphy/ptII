/* A code generation helper class for actor.lib.BooleanSwitch
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
 A code generation helper class for ptolemy.actor.lib.BooleanSwitch.

 @author Man-Kit Leung
 @version $Id$
 @since Ptolemy II 6.0
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
 */
public class BooleanSwitch extends CCodeGeneratorHelper {

    /**
     * Construct a BooleanSwitch helper.
     * @param actor The associated actor.
     */
    public BooleanSwitch(ptolemy.actor.lib.BooleanSwitch actor) {
        super(actor);
    }

    /**
     * Return the code generated for sending data to the true output port.
     * Iterate through the connections to the true port
     * and append the "trueBlock" code block from the template.
     * @return The generated code.
     * @exception IllegalActionException Thrown if an error occurs
     * when getting the width for the port or the code block.
     */
    public String generateTrueOutputs() throws IllegalActionException {
        ptolemy.actor.lib.BooleanSwitch actor = (ptolemy.actor.lib.BooleanSwitch) getComponent();

        int width = Math.min(actor.input.getWidth(),
                actor.trueOutput.getWidth());

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
     * Return the code generated for sending data to the false output port.
     * Iterate through the connections to the false port
     * and append the "falseBlock" code block from the template.
     * @return The generated code.
     * @exception IllegalActionException Thrown if an error occurs
     * when getting the width for the port or the code block.
     */
    public String generateFalseOutputs() throws IllegalActionException {
        ptolemy.actor.lib.BooleanSwitch actor = (ptolemy.actor.lib.BooleanSwitch) getComponent();

        int width = Math.min(actor.input.getWidth(),
                actor.trueOutput.getWidth());

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
