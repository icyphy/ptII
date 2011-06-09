/* A code generation helper class for actor.lib.ArrayAppend

 @Copyright (c) 2005-2009 The Regents of the University of California.
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
package ptolemy.codegen.c.actor.lib;

import java.util.ArrayList;

import ptolemy.codegen.c.kernel.CCodeGeneratorHelper;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.Type;
import ptolemy.kernel.util.IllegalActionException;

/**
 A code generation helper class for ptolemy.actor.lib.ArrayAppend.

 @author Man-Kit Leung
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Green (mankit)
 @Pt.AcceptedRating Green (cxh)
 */
public class ArrayAppend extends CCodeGeneratorHelper {

    /**
     * Construct an ArrayAppend helper.
     * @param actor The associated actor.
     */
    public ArrayAppend(ptolemy.actor.lib.ArrayAppend actor) {
        super(actor);
    }

    /**
     * Generate fire code.
     * The method reads in the <code>fireBlock</code> from ArrayAppend.c,
     * replaces macros with their values and returns the processed code
     * block.
     * @return The generated code.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    protected String _generateFireCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(super._generateFireCode());

        ptolemy.actor.lib.ArrayAppend actor = (ptolemy.actor.lib.ArrayAppend) getComponent();

        ArrayList args = new ArrayList();
        args.add(Integer.valueOf(0));
        for (int i = 0; i < actor.input.getWidth(); i++) {
            args.set(0, Integer.valueOf(i));
            code.append(_generateBlockCode("getTotalLength", args));
        }
        code.append(_generateBlockCode("allocNewArray"));

        args.add("");
        for (int i = 0; i < actor.input.getWidth(); i++) {
            args.set(0, Integer.valueOf(i));

            Type inputType = actor.input.getType();

            if (inputType instanceof ArrayType) {
                args.set(1, codeGenType(((ArrayType) inputType)
                        .getElementType()));
                code.append(_generateBlockCode("fillArray", args));

            } else {
                // This shouldn't happen because the type
                // constraints enforce this.
            }
        }

        code.append(_generateBlockCode("doDelete"));

        return processCode(code.toString());
    }
}
