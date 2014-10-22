/* A helper class for ptolemy.actor.lib.Sequence

 Copyright (c) 2006-2014 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

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
package ptolemy.cg.adapter.generic.program.procedural.adapters.ptolemy.actor.lib;

import ptolemy.cg.kernel.generic.program.CodeStream;
import ptolemy.cg.kernel.generic.program.NamedProgramCodeGeneratorAdapter;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// Sequence

/**
 A helper class for ptolemy.actor.lib.Sequence.

 @author Christopher Brooks, based on Sequence by Man-Kit Leung, Gang Zhou
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class Sequence extends NamedProgramCodeGeneratorAdapter {
    /**
     * Construct a Sequence adapter.
     * @param actor the associated actor
     */
    public Sequence(ptolemy.actor.lib.Sequence actor) {
        super(actor);
    }

    /**
     * Generate fire code.
     * The method reads in <code>codeBlock1</code>, <code>codeBlock2</code>,
     * and <code>codeBlock3</code> from Sequence.c, replaces macros with
     * their values and returns the processed code block.
     * @return The generated code.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    @Override
    protected String _generateFireCode() throws IllegalActionException {
        super._generateFireCode();
        ptolemy.actor.lib.Sequence actor = (ptolemy.actor.lib.Sequence) getComponent();
        CodeStream codeStream = _templateParser.getCodeStream();

        if (!actor.enable.isOutsideConnected()) {
            codeStream.appendCodeBlock("codeBlock1");
        } else {
            codeStream.appendCodeBlock("codeBlock2");
        }

        codeStream.appendCodeBlock("codeBlock3");
        return processCode(codeStream.toString());
    }
}
