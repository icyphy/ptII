/* A adapter class for ptolemy.actor.lib.MultiplyDivide

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

import java.util.ArrayList;

import ptolemy.cg.kernel.generic.program.CodeStream;
import ptolemy.cg.kernel.generic.program.NamedProgramCodeGeneratorAdapter;
import ptolemy.data.type.Type;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// MultiplyDivide

/**
 A adapter class for ptolemy.actor.lib.MultiplyDivide.

 @author Man-Kit (Jackie) Leung, Gang Zhou
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Green (mankit)
 @Pt.AcceptedRating Green (cxh)
 */
public class MultiplyDivide extends NamedProgramCodeGeneratorAdapter {
    /**
     * Constructor method for the MultiplyDivide adapter.
     * @param actor the associated actor
     */
    public MultiplyDivide(ptolemy.actor.lib.MultiplyDivide actor) {
        super(actor);
    }

    /**
     * Generate fire code.
     * The method generates code that loops through each
     * input [multi-ports] and combines (add or subtract) them.
     * @return The generated code.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    @Override
    protected String _generateFireCode() throws IllegalActionException {
        super._generateFireCode();

        ptolemy.actor.lib.MultiplyDivide actor = (ptolemy.actor.lib.MultiplyDivide) getComponent();

        String outputType = getCodeGenerator().codeGenType(
                actor.output.getType());
        String multiplyType = getCodeGenerator().codeGenType(
                actor.multiply.getType());
        String divideType = getCodeGenerator().codeGenType(
                actor.divide.getType());
        boolean divideOnly = !actor.multiply.isOutsideConnected();

        ArrayList<String> args = new ArrayList<String>();

        ArrayList<String> initArgs = new ArrayList<String>();

        // These lines are needed to handle
        // $PTII/bin/ptcg -language java $PTII/ptolemy/moml/filter/test/auto/modulation2.xml
        if (divideOnly) {
            initArgs.add(divideType);
        } else {
            initArgs.add(multiplyType);
            initArgs.add(outputType);
        }

        CodeStream codeStream = getTemplateParser().getCodeStream();
        codeStream.appendCodeBlock(divideOnly ? "divideOnlyInitProduct"
                : "initProduct", initArgs);

        args.add("");
        args.add(outputType);
        args.add(multiplyType);

        for (int i = 1; i < actor.multiply.getWidth(); i++) {
            args.set(0, Integer.toString(i));
            codeStream.appendCodeBlock("multiplyBlock", args);
        }

        for (int i = divideOnly ? 1 : 0; i < actor.divide.getWidth(); i++) {
            args.set(0, Integer.toString(i));
            args.set(2, divideType);
            codeStream.appendCodeBlock("divideBlock", args);
        }
        codeStream.appendCodeBlock("outputBlock");

        return processCode(codeStream.toString());
    }

    /**
     * Generate preinitialize code.
     * Read the <code>preinitBlock</code> from MultiplyDivide.c,
     * replace macros with their values and returns the processed code
     * block.
     * @return The generated code.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    @Override
    public String generatePreinitializeCode() throws IllegalActionException {
        super.generatePreinitializeCode();

        ptolemy.actor.lib.MultiplyDivide actor = (ptolemy.actor.lib.MultiplyDivide) getComponent();

        ArrayList args = new ArrayList();

        Type type = actor.output.getType();
        args.add(targetType(type));

        CodeStream codeStream = getTemplateParser().getCodeStream();

        if (codeStream.isEmpty()) {
            codeStream.append(_eol
                    + getCodeGenerator().comment(
                            "preinitialize "
                                    + generateSimpleName(getComponent())));
        }

        codeStream.appendCodeBlock("preinitBlock", args);

        return processCode(codeStream.toString());
    }
}
