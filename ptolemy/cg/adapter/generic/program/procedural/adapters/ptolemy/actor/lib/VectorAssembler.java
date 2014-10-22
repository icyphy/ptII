/* A adapter class for ptolemy.actor.lib.VectorAssembler

 @Copyright (c) 2007-2014 The Regents of the University of California.
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
package ptolemy.cg.adapter.generic.program.procedural.adapters.ptolemy.actor.lib;

import java.util.ArrayList;

import ptolemy.cg.kernel.generic.program.NamedProgramCodeGeneratorAdapter;
import ptolemy.data.type.Type;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// VectorDisassembler
/**
 * A adapter class for ptolemy.actor.lib.VectorAssembler.
 *
 * @see ptolemy.actor.lib.VectorAssembler
 * @author Christopher Brooks, based on codegen VectorAssembler by Man-Kit Leung
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class VectorAssembler extends NamedProgramCodeGeneratorAdapter {
    /**
     * Constructor method for the VectorAssembler helper.
     * @param actor the associated actor
     */
    public VectorAssembler(ptolemy.actor.lib.VectorAssembler actor) {
        super(actor);
    }

    /**
     * Generate fire code.
     * The method reads in <code>fireBlock</code> from VectorAssembler.c,
     * replaces macros with their values and returns the processed code
     * block.
     * @return The generated code.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    @Override
    protected String _generateFireCode() throws IllegalActionException {
        super._generateFireCode();

        ptolemy.actor.lib.VectorAssembler actor = (ptolemy.actor.lib.VectorAssembler) getComponent();

        StringBuffer fireCode = new StringBuffer();
        ArrayList<String> args = new ArrayList<String>();
        args.add("0");

        Type type = actor.input.getType();
        if (getCodeGenerator().isPrimitive(type)) {
            args.add(getCodeGenerator().codeGenType(type));
        }

        for (int i = 0; i < actor.input.getWidth(); i++) {
            args.set(0, Integer.toString(i));
            fireCode.append(getTemplateParser().generateBlockCode("fireBlock",
                    args));
        }

        return processCode(fireCode.toString());
    }
}
