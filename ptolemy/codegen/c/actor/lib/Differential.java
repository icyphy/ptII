/*
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
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
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
 * A helper class for ptolemy.actor.lib.Differential.
 *
 * @author Man-Kit Leung
 * @version $Id$
 * @since Ptolemy II 6.0
 * @Pt.ProposedRating Red (mankit)
 * @Pt.AcceptedRating Red (mankit)
 */
public class Differential extends CCodeGeneratorHelper {
    /**
     * Constructor method for the Differential helper.
     * @param actor the associated actor
     */
    public Differential(ptolemy.actor.lib.Differential actor) {
        super(actor);
    }

    /**
     * Generate fire code.
     * Read the <code>fireBlock</code> from Commutator.c,
     * replace macros with their values and append the processed code
     * block to the given code buffer.
     * @return The generated code.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    public String generateFireCode() throws IllegalActionException {
        super.generateFireCode();

        ptolemy.actor.lib.Differential actor = 
            (ptolemy.actor.lib.Differential) getComponent();

        ArrayList args = new ArrayList();
        args.add("");
        String type = codeGenType(actor.input.getType());
        args.add(type);
        for (int i = 0; i < actor.input.getWidth(); i++) {
            args.set(0, new Integer(i));
            String codeBlock;
            if (isPrimitive(type)) {
                if (isPrimitive(actor.output.getType())) {
                    codeBlock = "primitiveToPrimitiveFireBlock";
                } else {
                    codeBlock = "primitiveToTokenFireBlock";
                }
            } else {
                codeBlock = "tokenFireBlock";
            }
            _codeStream.appendCodeBlock(codeBlock, args);
        }
        return processCode(_codeStream.toString());
    }
}
