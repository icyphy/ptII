/* A helper class for actor.lib.conversions.Round

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
package ptolemy.codegen.c.actor.lib.conversions;

import java.util.Set;

import ptolemy.codegen.c.kernel.CCodeGeneratorHelper;
import ptolemy.kernel.util.IllegalActionException;

/**
 * A helper class for ptolemy.actor.lib.conversions.Round.
 *
 * @author Man-Kit Leung
 * @version $Id$
 * @since Ptolemy II 6.0
 * @Pt.ProposedRating Green (mankit)
 * @Pt.AcceptedRating Green (cxh)
 */
public class Round extends CCodeGeneratorHelper {
    /**
     * Constructor the Round helper.
     * @param actor the associated actor.
     */
    public Round(ptolemy.actor.lib.conversions.Round actor) {
        super(actor);
    }

    /**
     * Generate fire code.
     * The method reads in <code>ceilBlock</code>, <code>floorBlock</code>,
     * <code>roundBlock</code>, or <code>truncateBlock</code> from Round.c
     * depending on the function parameter specified, and
     * replace macros with their values and return the processed code
     * block.
     * @return The generated code.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    protected String _generateFireCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(super._generateFireCode());

        ptolemy.actor.lib.conversions.Round actor = (ptolemy.actor.lib.conversions.Round) getComponent();

        String function = actor.function.getExpression();
        String codeBlockName = (function.equals("ceil")) ? "ceilBlock"
                : ((function.equals("floor")) ? "floorBlock" : ((function
                        .equals("round")) ? "roundBlock" : "truncateBlock"));

        code.append(_generateBlockCode(codeBlockName));
        return code.toString();
    }

    /**
     * Get the files needed by the code generated for the
     * Round actor.
     * @return A set of strings that are names of the files
     *  needed by the code generated for the Round actor.
     * @exception IllegalActionException Not Thrown in this subclass.
     */
    public Set getHeaderFiles() throws IllegalActionException {
        Set files = super.getHeaderFiles();
        files.add("<limits.h>");
        files.add("<math.h>");
        return files;
    }
}
