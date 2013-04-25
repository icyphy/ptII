/* An adapter class for ptolemy.actor.lib.Ramp

 Copyright (c) 2006-2011 The Regents of the University of California.
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
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// Ramp

/**
 An adapter class for ptolemy.actor.lib.Ramp.

 @author Jia Zou, based on Ramp.java by Gang Zhou, Bert Rodiers
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (jiazou)
 @Pt.AcceptedRating Red (jiazou)
 */
public class Ramp extends NamedProgramCodeGeneratorAdapter {
    /**
     *  Construct the Ramp adapter.
     *  @param actor the associated actor
     */
    public Ramp(ptolemy.actor.lib.Ramp actor) {
        super(actor);
    }

    /** Generate the initialize code. Declare the variable state.
     *  @return The initialize code.
     *  @exception IllegalActionException
     */
    public String generateInitializeCode() throws IllegalActionException {
        super.generateInitializeCode();

        ptolemy.actor.lib.Ramp actor = (ptolemy.actor.lib.Ramp) getComponent();

        ArrayList<String> args = new ArrayList<String>();
        args.add(getCodeGenerator().codeGenType(actor.output.getType()));

        CodeStream codeStream = _templateParser.getCodeStream();
        codeStream.append(_eol
                + CodeStream.indent(getCodeGenerator().comment(
                        "initialize " + getComponent().getName())));
        if (actor.output.getType() == BaseType.STRING) {
            codeStream.appendCodeBlock("StringInitBlock");
        } else {
            codeStream.appendCodeBlock("CommonInitBlock", args);
            if (actor.output.getType() instanceof ArrayType) {
                Type elementType = ((ArrayType) actor.output.getType())
                        .getElementType();

                args.set(0,
                        "TYPE_" + getCodeGenerator().codeGenType(elementType));
                if (!actor.step.getType().equals(actor.output.getType())) {
                    codeStream.appendCodeBlock("ArrayConvertStepBlock", args);
                }
                if (!actor.init.getType().equals(actor.output.getType())) {
                    codeStream.appendCodeBlock("ArrayConvertInitBlock", args);
                }
            }
        }

        return processCode(codeStream.toString());
    }

    /**
     * Generate fire code for the Ramp actor.
     * @return The generated code.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    protected String _generateFireCode() throws IllegalActionException {
        super._generateFireCode();

        ptolemy.actor.lib.Ramp actor = (ptolemy.actor.lib.Ramp) getComponent();

        String type = getCodeGenerator().codeGenType(actor.output.getType());
        if (!getCodeGenerator().isPrimitive(type)) {
            type = "Token";
        }

        CodeStream codeStream = _templateParser.getCodeStream();
        codeStream.appendCodeBlock(type + "FireBlock");
        return processCode(codeStream.toString());
    }
}
