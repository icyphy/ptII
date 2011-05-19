/* A helper class for actor.lib.conversions.TokenToExpression

 @Copyright (c) 2006-2009 The Regents of the University of California.
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
package ptolemy.codegen.java.actor.lib.conversions;

import java.util.ArrayList;

import ptolemy.codegen.java.kernel.JavaCodeGeneratorHelper;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.kernel.util.IllegalActionException;

/**
 * A helper class for ptolemy.actor.lib.conversions.TokenToExpression.
 *
 * @author Christopher Brooks
 * @version $Id$
 * @since Ptolemy II 8.0
 * @Pt.ProposedRating Green (mankit)
 * @Pt.AcceptedRating Green (cxh)
 */
public class TokenToExpression extends JavaCodeGeneratorHelper {
    /**
     * Construct the TokenToExpression helper.
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
    protected String _generateFireCode() throws IllegalActionException {
        super._generateFireCode();

        ptolemy.actor.lib.conversions.TokenToExpression actor = (ptolemy.actor.lib.conversions.TokenToExpression) getComponent();

        Type inputType = actor.input.getType();
        if (isPrimitive(inputType)) {
            ArrayList args = new ArrayList();
            args.add(codeGenType(inputType));
            _codeStream.appendCodeBlock("FireBlock", args);
        } else {
            if (inputType instanceof ArrayType) {
                Type elementType = ((ArrayType) inputType).getElementType();

                if (elementType instanceof BaseType.ScalarType) {
                    // test/auto/AddSubtract4.xml needs this.
                    _codeStream.appendCodeBlock("TokenFireBlock");
                } else {
                    // test/auto/Commutator.xml needs this.
                    ArrayList args2 = new ArrayList();
                    args2.add("TYPE_" + codeGenType(elementType));
                    _codeStream.appendCodeBlock("TokenArrayFireBlock", args2);
                }
            } else {
                _codeStream.appendCodeBlock("TokenFireBlock");
            }
        }
        return processCode(_codeStream.toString());
    }
}
