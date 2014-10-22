/* An adapter class for actor.lib.conversions.TokenToExpression

 @Copyright (c) 2006-2014 The Regents of the University of California.
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

import java.util.ArrayList;

import ptolemy.cg.kernel.generic.program.CodeStream;
import ptolemy.cg.kernel.generic.program.NamedProgramCodeGeneratorAdapter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.ObjectType;
import ptolemy.data.type.Type;
import ptolemy.kernel.util.IllegalActionException;

/**
 * A helper class for ptolemy.actor.lib.conversions.TokenToExpression.
 *
 * @author Christopher Brooks
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class TokenToExpression extends NamedProgramCodeGeneratorAdapter {
    /**
     * Construct the TokenToExpression adapter.
     * @param actor the associated actor.
     */
    public TokenToExpression(
            ptolemy.actor.lib.conversions.TokenToExpression actor) {
        super(actor);
    }

    /**
     * Generate fire code.
     * Read the <code>fireBlock</code> from TokenToExpression.c,
     * replace macros with their values and return the processed code
     * block.
     * @return The generated code.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    @Override
    protected String _generateFireCode() throws IllegalActionException {
        super._generateFireCode();

        ptolemy.actor.lib.conversions.TokenToExpression actor = (ptolemy.actor.lib.conversions.TokenToExpression) getComponent();

        CodeStream codeStream = _templateParser.getCodeStream();
        Type inputType = actor.input.getType();
        if (getCodeGenerator().isPrimitive(inputType)) {
            ArrayList args = new ArrayList();
            args.add(getCodeGenerator().codeGenType(inputType));
            if (inputType instanceof ObjectType) {
                codeStream.appendCodeBlock("ObjectFireBlock", args);
            } else {
                codeStream.appendCodeBlock("FireBlock", args);
            }
        } else {
            if (inputType instanceof ArrayType) {
                Type elementType = ((ArrayType) inputType).getElementType();
                if (elementType instanceof BaseType.ScalarType) {
                    // test/auto/AddSubtract4.xml needs this.
                    codeStream.appendCodeBlock("TokenFireBlock");
                } else {
                    // test/auto/Commutator.xml needs this.
                    ArrayList args2 = new ArrayList();
                    args2.add("TYPE_"
                            + getCodeGenerator().codeGenType(elementType));
                    codeStream.appendCodeBlock("TokenArrayFireBlock", args2);
                }
            } else {
                codeStream.appendCodeBlock("TokenFireBlock");
            }
        }
        return processCode(codeStream.toString());
    }
}
