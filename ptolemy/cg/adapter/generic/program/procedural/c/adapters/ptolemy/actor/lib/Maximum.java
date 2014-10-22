/* An adapter class for ptolemy.actor.lib.Maximum
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
package ptolemy.cg.adapter.generic.program.procedural.c.adapters.ptolemy.actor.lib;

import java.util.ArrayList;

import ptolemy.cg.kernel.generic.program.CodeStream;
import ptolemy.cg.kernel.generic.program.NamedProgramCodeGeneratorAdapter;
import ptolemy.kernel.util.IllegalActionException;

/**
 * An adapter class for ptolemy.actor.lib.Maximum.
 *
 * @author Man-Kit Leung, Gang Zhou
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Green (cxh)
 * @Pt.AcceptedRating Green (mankit)
 */
public class Maximum extends NamedProgramCodeGeneratorAdapter {
    /**
     * Constructor a Maximum adapter.
     * @param actor the associated actor
     */
    public Maximum(ptolemy.actor.lib.Maximum actor) {
        super(actor);
    }

    /**
     * Generate fire code.
     * The method reads in <code>fireBlock</code> from Maximum.c,
     * replaces macros with their values and returns the processed code
     * block.
     * @return The generated code.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    @Override
    protected String _generateFireCode() throws IllegalActionException {
        super._generateFireCode();

        ptolemy.actor.lib.Maximum actor = (ptolemy.actor.lib.Maximum) getComponent();

        CodeStream codeStream = getTemplateParser().getCodeStream();

        codeStream.append("while (true) {" + _eol);
        for (int i = 0; i < actor.input.getWidth(); i++) {
            ArrayList args = new ArrayList();
            args.add(Integer.toString(i));
            codeStream.appendCodeBlock("fireInitBlock", args);
        }
        codeStream.append("}" + _eol);
        // FIXME: we need to resolve other ScalarTokens like Complex.
        for (int i = 0; i < actor.input.getWidth(); i++) {
            ArrayList args = new ArrayList();
            args.add(Integer.toString(i));
            codeStream.appendCodeBlock("findBlock", args);
        }
        for (int i = 0; i < actor.maximumValue.getWidth(); i++) {
            ArrayList args = new ArrayList();
            args.add(Integer.toString(i));
            codeStream.appendCodeBlock("sendBlock1", args);
        }
        for (int i = 0; i < actor.channelNumber.getWidth(); i++) {
            ArrayList args = new ArrayList();
            args.add(Integer.toString(i));
            codeStream.appendCodeBlock("sendBlock2", args);
        }
        return processCode(codeStream.toString());
    }
}
