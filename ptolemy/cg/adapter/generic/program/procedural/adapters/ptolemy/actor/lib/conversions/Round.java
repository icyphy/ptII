/* A helper class for actor.lib.conversions.Round

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
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY


 */
package ptolemy.cg.adapter.generic.program.procedural.adapters.ptolemy.actor.lib.conversions;

import ptolemy.cg.kernel.generic.program.CodeStream;
import ptolemy.cg.kernel.generic.program.NamedProgramCodeGeneratorAdapter;
import ptolemy.kernel.util.IllegalActionException;

/**
 * A helper class for ptolemy.actor.lib.conversions.Round.
 *
 * @author Man-Kit Leung
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class Round extends NamedProgramCodeGeneratorAdapter {
    /**
     * Constructor the Round helper.
     * @param actor the associated actor.
     */
    public Round(ptolemy.actor.lib.conversions.Round actor) {
        super(actor);
    }

    /**
     * Generate fire code.
     * The method reads in <code>ceilBlock</code>, <code>floorBlock</code>,
     * <code>roundBlock</code>, or <code>truncateBlock</code> from Round.c
     * depending on the function parameter specified, and
     * replace macros with their values and return the processed code
     * block.
     * @return The generated code.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    @Override
    protected String _generateFireCode() throws IllegalActionException {
        //StringBuffer code = new StringBuffer();
        //code.append(super._generateFireCode());
        super._generateFireCode();

        ptolemy.actor.lib.conversions.Round actor = (ptolemy.actor.lib.conversions.Round) getComponent();

        String function = actor.function.getExpression();
        String codeBlockName = function.equals("ceil") ? "ceilBlock" : function
                .equals("floor") ? "floorBlock"
                        : function.equals("round") ? "roundBlock" : "truncateBlock";

        //code.append(getTemplateParser().generateBlockCode(codeBlockName, null));
        //return code.toString();
        CodeStream codeStream = _templateParser.getCodeStream();
        codeStream.appendCodeBlock(codeBlockName);
        return processCode(codeStream.toString());

    }
}
