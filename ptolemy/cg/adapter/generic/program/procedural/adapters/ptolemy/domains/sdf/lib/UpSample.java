/* A code generation adapter class for domains.sdf.lib.UpSample

 @Copyright (c) 2007-2010 The Regents of the University of California.
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
package ptolemy.cg.adapter.generic.program.procedural.adapters.ptolemy.domains.sdf.lib;

import java.util.ArrayList;

import ptolemy.cg.kernel.generic.program.CodeStream;
import ptolemy.cg.kernel.generic.program.NamedProgramCodeGeneratorAdapter;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.kernel.util.IllegalActionException;

/**
 A code generation adapter class for ptolemy.domains.sdf.lib.UpSample.

 @author Man-Kit Leung, Dai Bui, Bert Rodiers
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
 */
public class UpSample extends NamedProgramCodeGeneratorAdapter {

    /**
     * Construct a UpSample adapter.
     * @param actor The associated actor.
     */
    public UpSample(ptolemy.domains.sdf.lib.UpSample actor) {
        super(actor);
    }

    /**
     * Generate fire code.
     * Read the <code>fireBlock</code> from UpSample.c,
     * replace macros with their values and return the processed code string.
     * @return The processed code string.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    @Override
    protected String _generateFireCode() throws IllegalActionException {
        CodeStream codeStream = _templateParser.getCodeStream();

        ptolemy.domains.sdf.lib.UpSample actor = (ptolemy.domains.sdf.lib.UpSample) getComponent();

        ArrayList args = new ArrayList();

        Type type = actor.input.getType();
        if (!getCodeGenerator().isPrimitive(type)) {
            if (type == BaseType.GENERAL) {
                //                args.add("$typeFunc($ref(input).type::zero())");
                args.add("$typeFunc($get(input).type::zero())");
            } else {
                args.add(getCodeGenerator().codeGenType(type) + "_zero()");
            }
        } else {
            args.add("0");
        }

        codeStream.appendCodeBlock("fireBlock", args);
        return codeStream.toString();
    }

    /**
     * Generate preinitialize code.
     * Read the <code>preinitBlock</code> from the template file.
     * replace macros with their values and returns the processed code
     * block.
     * @return The generated code.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    @Override
    public String generatePreinitializeCode() throws IllegalActionException {
        super.generatePreinitializeCode();

        ptolemy.domains.sdf.lib.UpSample actor = (ptolemy.domains.sdf.lib.UpSample) getComponent();

        ArrayList<String> args = new ArrayList<String>();

        Type type = actor.input.getType();
        args.add(targetType(type));

        CodeStream codeStream = _templateParser.getCodeStream();

        if (codeStream.isEmpty()) {
            codeStream.append(_eol + getCodeGenerator()
                    .comment("preinitialize " + getComponent().getName()));
        }

        codeStream.appendCodeBlock("preinitBlock", args);

        return processCode(codeStream.toString());
    }
}
