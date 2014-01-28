/* A code generation helper class for actor.lib.Distributor

 @Copyright (c) 2005-2011 The Regents of the University of California.
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
package ptolemy.cg.adapter.generic.program.procedural.adapters.ptolemy.actor.lib;

import java.util.ArrayList;

import ptolemy.cg.kernel.generic.program.CodeStream;
import ptolemy.cg.kernel.generic.program.NamedProgramCodeGeneratorAdapter;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.kernel.util.IllegalActionException;

/**
 * A code generation helper class for ptolemy.actor.lib.Distributor.
 *
 * @author Christopher Brooks, based on codegen Distributor by Man-Kit Leung
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class Distributor extends NamedProgramCodeGeneratorAdapter {
    /**
     * Construct the Distributor helper.
     * @param actor The associated actor.
     */
    public Distributor(ptolemy.actor.lib.Distributor actor) {
        super(actor);
    }

    /**
     * Generate fire code.
     * Read the <code>fireBlock</code> from Distributor.c,
     * replace macros with their values and return the processed code
     * block.
     * @return The generated code.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    protected String _generateFireCode() throws IllegalActionException {
        super._generateFireCode();

        ptolemy.actor.lib.Distributor actor = (ptolemy.actor.lib.Distributor) getComponent();
        CodeStream codeStream = _templateParser.getCodeStream();

        ArrayList<String> args = new ArrayList<String>();

        args.add(Integer.toString(0));
        Type inputType = actor.input.getType();
        args.add(inputType.toString());
        for (int i = 0; i < actor.output.getWidth(); i++) {
            args.set(0, Integer.toString(i));

            String codeBlock = "";
            if (getCodeGenerator().isPrimitive(inputType)
                    && !getCodeGenerator().isPrimitive(actor.output.getType())) {
                codeBlock = "toTokenBlock";
            } else {
                if (actor.output.getType() == BaseType.STRING) {
                    if (inputType == BaseType.INT) {
                        codeBlock = "IntToStringBlock";
                    } else if (inputType == BaseType.DOUBLE) {
                        codeBlock = "DoubleToStringBlock";
                    } else if (inputType == BaseType.LONG) {
                        codeBlock = "LongToStringBlock";
                    } else if (inputType == BaseType.BOOLEAN) {
                        codeBlock = "BooleanToStringBlock";
                    } else {
                        throw new IllegalActionException(
                                "Unhandled input type to string");
                    }
                } else {
                    codeBlock = "assignBlock";
                }
            }
            codeStream.appendCodeBlock(codeBlock, args);
        }
        return processCode(codeStream.toString());
    }
}
