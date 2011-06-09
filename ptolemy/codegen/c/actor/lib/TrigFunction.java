/* A helper class for ptolemy.actor.lib.TrigFunction.

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
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY


 */
package ptolemy.codegen.c.actor.lib;

import java.util.Set;

import ptolemy.codegen.c.kernel.CCodeGeneratorHelper;
import ptolemy.kernel.util.IllegalActionException;

/**
 * A helper class for ptolemy.actor.lib.TrigFunction.
 *
 * @author Man-Kit Leung
 * @version $Id$
 * @since Ptolemy II 6.0
 * @Pt.ProposedRating Green (cxh)
 * @Pt.AcceptedRating Green (cxh)
 */
public class TrigFunction extends CCodeGeneratorHelper {
    /**
     * Constructor method for the TrigFunction helper.
     * @param actor the associated actor.
     */
    public TrigFunction(ptolemy.actor.lib.TrigFunction actor) {
        super(actor);
    }

    /**
     * Generate fire code.
     * The method reads in <code>sinBlock</code>, <code>cosBlock</code>,
     * <code>tanBlock</code>, <code>asinBlock</code>, <code>acosBlock</code>,
     * or <code>atanBlock</code> from TrigFunction.c depending on the function
     * parameter specified, replaces macros with their values and appends
     * the processed code block to the given code buffer.
     * @return The generated code.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    protected String _generateFireCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(super._generateFireCode());

        ptolemy.actor.lib.TrigFunction actor = (ptolemy.actor.lib.TrigFunction) getComponent();

        String function = actor.function.getExpression();
        String codeBlockName = (function.equals("sin")) ? "sinBlock"
                : ((function.equals("cos")) ? "cosBlock" : ((function
                        .equals("tan")) ? "tanBlock" : ((function
                        .equals("asin")) ? "asinBlock" : ((function
                        .equals("acos")) ? "acosBlock" : "atanBlock"))));
        code.append(_generateBlockCode(codeBlockName));

        return code.toString();
    }

    /**
     * Get the files needed by the code generated for the
     * TrigFunction actor.
     * @return A set of strings that are names of the files
     *  needed by the code generated for the TrigFunction actor.
     * @exception IllegalActionException Not Thrown in this subclass.
     */
    public Set getHeaderFiles() throws IllegalActionException {
        Set files = super.getHeaderFiles();
        files.add("<math.h>");
        return files;
    }
}
