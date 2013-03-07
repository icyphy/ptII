/* A adapter class for ptolemy.actor.lib.Test

 Copyright (c) 2005-2013 The Regents of the University of California.
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
import ptolemy.cg.kernel.generic.program.procedural.ProceduralCodeGenerator;
import ptolemy.cg.kernel.generic.program.procedural.ProceduralTemplateParser;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// Test

/**
 A adapter class for ptolemy.actor.lib.Test.

 @author Christopher Brooks
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Green (cxh)
 @Pt.AcceptedRating Green (cxh)
 */
public class Test extends NamedProgramCodeGeneratorAdapter {
    /**
     *  Construct a Test adapter.
     *  @param actor The master Test actor.
     */
    public Test(ptolemy.actor.lib.Test actor) {
        super(actor);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Generate fire code.
     * The method reads in <code>fireBlock</code> from Test.c,
     * replaces macros with their values and returns the processed code
     * block.
     * @return The generated code.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    protected String _generateFireCode() throws IllegalActionException {
        super._generateFireCode();

        ptolemy.actor.lib.Test actor = (ptolemy.actor.lib.Test) getComponent();

        ArrayList<String> args = new ArrayList<String>();
        args.add(Integer.valueOf(0).toString());
        String multiChannel = "";
        String inputType = "";

        CodeStream codeStream = _templateParser.getCodeStream();

        if (actor.input.getWidth() > 1) {
            // If we have multiple inputs, use different blocks
            multiChannel = "MultiChannel";
            //args.add(codeGenType(actor.input.getType()));
        }
        for (int i = 0; i < actor.input.getWidth(); i++) {
            args.set(0, Integer.valueOf(i).toString());
            if (getCodeGenerator().isPrimitive(actor.input.getType())) {
                inputType = getCodeGenerator().codeGenType(
                        actor.input.getType());
            } else if (actor.input.getType().toString().equals("complex")) {
                inputType = getCodeGenerator().codeGenType(
                        actor.input.getType());
            } else {
                inputType = "Token";
                ((ProceduralCodeGenerator) getCodeGenerator())
                        .markFunctionCalled("equals_Token_Token",
                                (ProceduralTemplateParser) _templateParser);
                //((ProceduralCodeGenerator) getCodeGenerator()).markFunctionCalled(
                //        "isCloseTo_Token_Token",
                //        (ProceduralTemplateParser) _templateParser);
            }

            codeStream
                    .appendCodeBlock(inputType + "Block" + multiChannel, args);
        }
        return processCode(codeStream.toString());
    }

    /** Generate the initialize code. Declare the variable state.
     *  @return The initialize code.
     *  @exception IllegalActionException
     */
    public String generateInitializeCode() throws IllegalActionException {
        super.generateInitializeCode();

        CodeStream codeStream = _templateParser.getCodeStream();

        ptolemy.actor.lib.Test actor = (ptolemy.actor.lib.Test) getComponent();
        for (int i = 0; i < actor.input.getWidth(); i++) {
            if (!getCodeGenerator().isPrimitive(actor.input.getType())) {
                // One of the channels is not primitive, so we will
                // later call TokenBlock($channel), so we define
                // toleranceToken for our use.
                codeStream.appendCodeBlock("toleranceTokenInitBlock");
                break;
            }
        }
        return processCode(codeStream.toString());
    }

    /**
     * Generate the preinitialize code. Declare temporary variables.
     * @return The preinitialize code.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    public String generatePreinitializeCode() throws IllegalActionException {
        // Automatically append the "preinitBlock" by default.
        super.generatePreinitializeCode();

        ptolemy.actor.lib.Test actor = (ptolemy.actor.lib.Test) getComponent();

        CodeStream codeStream = _templateParser.getCodeStream();

        if (actor.input.getWidth() > 1) {
            ArrayList<String> args = new ArrayList<String>();
            args.add(Integer.valueOf(0).toString());
            for (int i = 0; i < actor.input.getWidth(); i++) {
                args.set(0, Integer.valueOf(i).toString());
                codeStream.appendCodeBlock("TokenPreinitBlock", args);
            }
        }

        for (int i = 0; i < actor.input.getWidth(); i++) {
            if (!getCodeGenerator().isPrimitive(actor.input.getType())) {
                // One of the channels is not primitive, so we will
                // later call TokenBlock($channel), so we define
                // toleranceToken for our use.
                codeStream.appendCodeBlock("toleranceTokenPreinitBlock");
                break;
            }
        }

        return processCode(codeStream.toString());
    }

}
