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
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.kernel.util.IllegalActionException;

/**
 * A code generation helper class for ptolemy.actor.lib.Distributor.
 *
 * @author Christopher Brooks
 * @version $Id$
 * @since Ptolemy II 6.0
 * @Pt.ProposedRating Red (mankit)
 * @Pt.AcceptedRating Red (mankit)
 */
public class Distributor extends CCodeGeneratorHelper {
    /**
     * Construct the Distributor helper.
     * @param actor The associated actor.
     */
    public Distributor(ptolemy.actor.lib.Distributor actor) {
        super(actor);
    }

    /**
     * Generate fire code.
     * Read the <code>fireBlock</code> from Distributor.c,
     * replace macros with their values and return the processed code
     * block.
     * @return The generated code.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    public String generateFireCode() throws IllegalActionException {
        super.generateFireCode();

        ptolemy.actor.lib.Distributor actor = (ptolemy.actor.lib.Distributor) getComponent();

        ArrayList args = new ArrayList();
        args.add(new Integer(0));
        Type inputType = actor.input.getType();
        args.add(inputType);
        for (int i = 0; i < actor.output.getWidth(); i++) {
            args.set(0, new Integer(i));

            String codeBlock = "";
            if (isPrimitive(inputType) && !isPrimitive(actor.output.getType())) {
                codeBlock = "toTokenBlock";
            } else {
                if (actor.output.getType() == BaseType.STRING) {
                    if (inputType == BaseType.INT) {
                        codeBlock = "IntToStringBlock";
                    } else if (inputType == BaseType.DOUBLE) {
                        codeBlock = "DoubleToStringBlock";
                    } else if (inputType == BaseType.LONG) {
                        codeBlock = "LongToStringBlock";
                    } else if (inputType == BaseType.BOOLEAN) {
                        codeBlock = "BooleanToStringBlock";
                    } else {
                        throw new IllegalActionException(
                                "Unhandled input type to string");
                    }
                } else {
                    codeBlock = "assignBlock";
                }
            }
            _codeStream.appendCodeBlock(codeBlock, args);
        }
        return processCode(_codeStream.toString());
    }
}
