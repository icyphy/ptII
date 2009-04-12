/* A code generation helper class for domains.sdf.lib.DotProduct

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
package ptolemy.codegen.c.domains.sdf.lib;

import java.util.ArrayList;

import ptolemy.codegen.c.kernel.CCodeGeneratorHelper;
import ptolemy.data.type.ArrayType;
import ptolemy.kernel.util.IllegalActionException;

/**
 A code generation helper class for ptolemy.domains.sdf.lib.DotProduct.

 @author Man-Kit Leung
 @version $Id$
 @since Ptolemy II 6.0
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
 */
public class DotProduct extends CCodeGeneratorHelper {

    /**
     * Construct a DotProduct helper.
     * @param actor The associated actor.
     */
    public DotProduct(ptolemy.domains.sdf.lib.DotProduct actor) {
        super(actor);
    }

    /**
     * Generate fire code.
     * Read the <code>fireBlock</code> from DotProduct.c,
     * replace macros with their values and return the processed code
     * block.
     * @return The processed code string.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    protected String _generateFireCode() throws IllegalActionException {
        super._generateFireCode();

        ptolemy.domains.sdf.lib.DotProduct actor = (ptolemy.domains.sdf.lib.DotProduct) getComponent();

        ArrayList args = new ArrayList();

        if (actor.input1.getType() instanceof ArrayType) {
            args.add(codeGenType(((ArrayType) actor.input1.getType())
                    .getElementType()));
        } else {
            throw new IllegalActionException("Unhandled type for input1: ("
                    + actor.input1.getType() + ")");
        }

        if (actor.input2.getType() instanceof ArrayType) {
            args.add(codeGenType(((ArrayType) actor.input2.getType())
                    .getElementType()));
        } else {
            throw new IllegalActionException("Unhandled type for input2: ("
                    + actor.input2.getType() + ")");
        }

        _codeStream.appendCodeBlock("fireBlock", args);

        return processCode(_codeStream.toString());
    }
}
