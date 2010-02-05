/* A code generation helper class for actor.lib.gui.Display

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
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY

 */
package ptolemy.codegen.c.actor.lib.gui;

import java.util.ArrayList;
import java.util.Set;

import ptolemy.codegen.c.kernel.CCodeGeneratorHelper;
import ptolemy.kernel.util.IllegalActionException;

/**
 * A code generation helper class for ptolemy.actor.lib.gui.Display.
 *
 * @author Man-Kit Leung
 * @version $Id$
 * @since Ptolemy II 6.0
 * @Pt.ProposedRating Green (mankit)
 * @Pt.AcceptedRating Green (mankit)
 */
public class Display extends CCodeGeneratorHelper {
    /**
     * Construct a Display helper.
     * @param actor The associated actor.
     */
    public Display(ptolemy.actor.lib.gui.Display actor) {
        super(actor);
    }

    /**
     * Generate the postfire code.
     * The method reads in <code>printInt</code>, <code>printArray</code>,
     * <code>printString</code>, or <code>printDouble</code> from Display.c,
     * replaces macros with their values and returns the results.
     * @return The generated code.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    public String generatePostfireCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(super.generatePostfireCode());

        ptolemy.actor.lib.gui.Display actor = (ptolemy.actor.lib.gui.Display) getComponent();

        String type = codeGenType(actor.input.getType());
        if (!isPrimitive(type)) {
            type = "Token";
        }

        ArrayList args = new ArrayList();

        String title = actor.title.getExpression();
        if (title.trim().length() > 0) {
            args.add(title);
        } else {
            args.add(generateSimpleName(actor));
        }

        args.add(Integer.valueOf(0));
        for (int i = 0; i < actor.input.getWidth(); i++) {
            args.set(1, Integer.toString(i));
            code.append(_generateBlockCode(type + "PrintBlock", args));
        }

        return code.toString();
    }

    /**
     * Get the files needed by the code generated for the
     * Display actor.
     * @return A set of strings that are names of the header files
     *  needed by the code generated for the Display actor.
     * @exception IllegalActionException Not Thrown in this subclass.
     */
    public Set getHeaderFiles() throws IllegalActionException {
        Set files = super.getHeaderFiles();
        files.add("<math.h>");
        return files;
    }
}
