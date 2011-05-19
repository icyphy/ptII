/* A code generation helper class for domains.sdf.lib.UpSample
 @Copyright (c) 2007-2009 The Regents of the University of California.
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
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.kernel.util.IllegalActionException;

/**
 A code generation helper class for ptolemy.domains.sdf.lib.UpSample.

 @author Man-Kit Leung
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
 */
public class UpSample extends CCodeGeneratorHelper {

    /**
     * Construct a UpSample helper.
     * @param actor The associated actor.
     */
    public UpSample(ptolemy.domains.sdf.lib.UpSample actor) {
        super(actor);
    }

    /**
     * Generate fire code.
     * Read the <code>fireBlock</code> from UpSample.c,
     * replace macros with their values and return the processed code string.
     * @return The processed code string.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    protected String _generateFireCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(super._generateFireCode());

        ArrayList args = new ArrayList();

        ptolemy.domains.sdf.lib.UpSample actor = (ptolemy.domains.sdf.lib.UpSample) getComponent();

        Type type = actor.input.getType();
        if (!isPrimitive(type)) {
            if (type == BaseType.GENERAL) {
                args.add("$typeFunc($ref(input).type::zero())");
            } else {
                args.add(codeGenType(type) + "_zero()");
            }
        } else {
            args.add("0");
        }

        code.append(_generateBlockCode("fireBlock", args));
        return code.toString();
    }
}
