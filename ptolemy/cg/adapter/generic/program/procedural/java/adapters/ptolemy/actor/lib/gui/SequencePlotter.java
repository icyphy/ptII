/* A code generation helper class for actor.lib.gui.SequencPlotter

 @Copyright (c) 2009-2014 The Regents of the University of California.
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
package ptolemy.cg.adapter.generic.program.procedural.java.adapters.ptolemy.actor.lib.gui;

import java.util.ArrayList;

import ptolemy.cg.kernel.generic.program.NamedProgramCodeGeneratorAdapter;
import ptolemy.kernel.util.IllegalActionException;

/**
 * A code generation adapter class for ptolemy.actor.lib.gui.SequencePlotter.
 * This implementation is a simple implementation of the plotter. It actually
 * just lists the values.
 *
 * @author Bert Rodiers, Christopher Brooks
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (rodiers)
 * @Pt.AcceptedRating Red (rodiers)
 */
public class SequencePlotter extends NamedProgramCodeGeneratorAdapter {
    /**
     * Construct a Sequence Plotter helper.
     * @param actor The associated actor.
     */
    public SequencePlotter(ptolemy.actor.lib.gui.SequencePlotter actor) {
        super(actor);
    }

    /**
     * Generate fire code.
     * The method reads in <code>printInt</code>, <code>printArray</code>,
     * <code>printString</code>, or <code>printDouble</code> from SequencePlotter.j,
     * replaces macros with their values and returns the results.
     * @return The generated code.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    @Override
    protected String _generateFireCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(super._generateFireCode());

        ptolemy.actor.lib.gui.SequencePlotter actor = (ptolemy.actor.lib.gui.SequencePlotter) getComponent();

        String type = getCodeGenerator().codeGenType(actor.input.getType());
        if (!getCodeGenerator().isPrimitive(type)) {
            type = "Token";
        }

        ArrayList args = new ArrayList();

        args.add(generateSimpleName(actor));

        args.add(Integer.valueOf(0));
        for (int i = 0; i < actor.input.getWidth(); i++) {
            args.set(1, Integer.toString(i));
            code.append(_templateParser.generateBlockCode(type + "PrintBlock",
                    args));
        }

        return code.toString();
    }
}
