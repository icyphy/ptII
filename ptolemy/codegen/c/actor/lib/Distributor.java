/* A code generation helper class for actor.lib.Distributor
 @Copyright (c) 2005-2006 The Regents of the University of California.
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
import ptolemy.kernel.util.IllegalActionException;

/**
 * A code generation helper class for ptolemy.actor.lib.Distributor.
 *
 * @author Christopher Brooks
 * @version $Id$
 * @since Ptolemy II 5.1
 * @Pt.ProposedRating Red (mankit)
 * @Pt.AcceptedRating Red (mankit)
 */
public class Distributor extends CCodeGeneratorHelper {
    /**
     * Constructor method for the Distributor helper.
     * @param actor The associated actor.
     */
    public Distributor(ptolemy.actor.lib.Distributor actor) {
        super(actor);
    }

    /**
     * Generate fire code.
     * Read the <code>fireBlock</code> from Distributor.c,
     * replace macros with their values and append the processed code
     * block to the given code buffer.
     * @return The generated code.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    public String generateFireCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(super.generateFireCode());

        ptolemy.actor.lib.Distributor actor = (ptolemy.actor.lib.Distributor) getComponent();

        ArrayList args = new ArrayList();
        args.add("");
        String type = _codeGenType(actor.input.getType());
        args.add(type);
        for (int i = 0; i < actor.output.getWidth(); i++) {
            args.set(0, new Integer(i));
            String codeBlock;
            if (_isPrimitiveType(type)) {
                if (_isPrimitiveType(actor.output.getType())) {
                    codeBlock = "primitiveToPrimitiveFireBlock";
                } else {
                    codeBlock = "primitiveToTokenFireBlock";
                }
            } else {
                codeBlock = "tokenFireBlock";
            }
            code.append("/* " + codeBlock + " */");
            code.append(_generateBlockCode(codeBlock, args));
        }
        return processCode(code.toString());
    }
}
