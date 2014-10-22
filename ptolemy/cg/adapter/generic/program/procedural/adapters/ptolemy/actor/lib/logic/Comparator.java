/* A code generation adapter class for actor.lib.logic.Comparator

 @Copyright (c) 2005-2014 The Regents of the University of California.
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
package ptolemy.cg.adapter.generic.program.procedural.adapters.ptolemy.actor.lib.logic;

import ptolemy.cg.kernel.generic.program.CodeStream;
import ptolemy.cg.kernel.generic.program.NamedProgramCodeGeneratorAdapter;
import ptolemy.kernel.util.IllegalActionException;

/**
 A code generation adapter class for ptolemy.actor.lib.logic.Comparator.

 @author Man-Kit Leung
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Green (mankit)
 @Pt.AcceptedRating Green (mankit)
 */
public class Comparator extends NamedProgramCodeGeneratorAdapter {

    /**
     * Construct the Comparator adapter.
     * @param actor The associated actor.
     */
    public Comparator(ptolemy.actor.lib.logic.Comparator actor) {
        super(actor);
    }

    /**
     * Generate fire code.
     * Read the <code>fireBlock</code> from Comparator.c,
     * replace macros with their values and return the processed code
     * block.
     * @return The processed code string.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    @Override
    protected String _generateFireCode() throws IllegalActionException {
        super._generateFireCode();
        ptolemy.actor.lib.logic.Comparator actor = (ptolemy.actor.lib.logic.Comparator) getComponent();

        String comparison = actor.comparison.getExpression();

        CodeStream codeStream = getTemplateParser().getCodeStream();
        if (comparison.equals(">")) {
            codeStream.appendCodeBlock("GTBlock");
        } else if (comparison.equals(">=")) {
            codeStream.appendCodeBlock("GEBlock");
        } else if (comparison.equals("<")) {
            codeStream.appendCodeBlock("LTBlock");
        } else if (comparison.equals("<=")) {
            codeStream.appendCodeBlock("LEBlock");
        } else if (comparison.equals("==")) {
            codeStream.appendCodeBlock("EQBlock");
        }
        return processCode(codeStream.toString());
    }
}
