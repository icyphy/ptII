/* A adapter class for ptolemy.actor.lib.VectorDisassembler

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

import ptolemy.cg.kernel.generic.program.CodeStream;
import ptolemy.cg.kernel.generic.program.NamedProgramCodeGeneratorAdapter;
import ptolemy.data.type.Type;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// VectorDisassembler
/**
 A adapter class for VectorDisassembler.

 @author Christopher Brooks, based on codegen VectorDisassembler by Man-Kit Leung
 @version Id: VectorAssembler.java 57046 2010-01-27 23:35:53Z cxh $
 @since Ptolemy II 10.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class VectorDisassembler extends NamedProgramCodeGeneratorAdapter {
    /**
     * Constructor method for the VectorDisassembler helper.
     * @param actor the associated actor
     */
    public VectorDisassembler(ptolemy.actor.lib.VectorDisassembler actor) {
        super(actor);
    }

    /**
     * Generate fire code.
     * The method reads in <code>fireBlock</code> from VectorDisassembler.c,
     * replaces macros with their values and returns the processed code
     * block.
     * @return The generated code.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    @Override
    protected String _generateFireCode() throws IllegalActionException {
        super._generateFireCode();

        ptolemy.actor.lib.VectorDisassembler actor = (ptolemy.actor.lib.VectorDisassembler) getComponent();

        StringBuffer fireCode = new StringBuffer();
        ArrayList<String> args = new ArrayList<String>();
        args.add(Integer.toString(actor.output.getWidth()));
        fireCode.append(getTemplateParser()
                .generateBlockCode("fireBlock", args));

        //         Type type = actor.output.getType();
        //         if (isPrimitive(type)) {
        //             args.add(codeGenType(type));
        //         }

        //         FIXME: we need a way to get the matrix dimensions here
        //         DoubleMatrixToken vector = (DoubleMatrixToken) actor.input.get(0);

        //         if (vector.getColumnCount() != 1) {
        //             throw new IllegalActionException(this, "The input must "
        //                     + "be a DoubleMatrixToken with one column.");
        //         }

        //         int min = Math.min(vector.getRowCount(), actor.output.getWidth());
        //        int min = actor.output.getWidth();
        //         int min = Math.min(actor.input.getWidth(), actor.output.getWidth());
        //         for (int i = 0; i < min; i++) {
        //             args.set(0, Integer.valueOf(i));
        //             fireCode.append(getTemplateParser().generateBlockCode("fireBlock", args));
        //         }

        ArrayList<String> args2 = new ArrayList<String>();
        Type type = actor.output.getType();
        if (getCodeGenerator().isPrimitive(type)) {
            args2.add(getCodeGenerator().codeGenType(type));
            fireCode.append(getTemplateParser().generateBlockCode("fireBlock2",
                    args2));
        } else {
            fireCode.append(getTemplateParser().generateBlockCode("fireBlock2",
                    args2));
        }

        ArrayList<String> args3 = new ArrayList<String>();
        args3.add(Integer.toString(0));

        for (int i = 0; i < actor.output.numberOfSinks(); i++) {
            args3.set(0, Integer.toString(i));
            fireCode.append(getTemplateParser().generateBlockCode("fireBlock3",
                    args3));
        }
        return processCode(fireCode.toString());
    }

    /**
     * Generate preinitialize code.
     * Read the <code>preinitBlock</code>,
     * replace macros with their values and return the processed code string.
     * @return The processed code string.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    @Override
    public String generatePreinitializeCode() throws IllegalActionException {
        super.generatePreinitializeCode();
        ptolemy.actor.lib.VectorDisassembler actor = (ptolemy.actor.lib.VectorDisassembler) getComponent();

        ArrayList<String> args = new ArrayList<String>();
        CodeStream codeStream = _templateParser.getCodeStream();

        args.add(Integer.toString(actor.output.getWidth()));

        codeStream.appendCodeBlock("preinitBlock", args);
        return processCode(codeStream.toString());
    }
}
