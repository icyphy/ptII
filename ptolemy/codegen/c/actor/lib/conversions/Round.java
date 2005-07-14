/* A helper class for actor.lib.conversions.Round

 @Copyright (c) 2005 The Regents of the University of California.
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

import java.util.HashSet;
import java.util.Set;

import ptolemy.codegen.c.actor.lib.CodeStream;
import ptolemy.codegen.kernel.CCodeGeneratorHelper;
import ptolemy.kernel.util.IllegalActionException;

/**
 * A helper class for ptolemy.actor.lib.conversions.Round.
 * 
 * @author Man-Kit Leung
 * @version $Id$
 * @since Ptolemy II 5.1
 * @Pt.ProposedRating Red (mankit)
 * @Pt.AcceptedRating Red (mankit)
 */
public class Round extends CCodeGeneratorHelper {

    /**
     * Constructor method for the Round helper.
     * @param actor the associated actor.
     */
    public Round(ptolemy.actor.lib.conversions.Round actor) {
        super(actor);
    }

    /**
     * Generate fire code
     * The method reads in <code>ceilBlock</code>, <code>floorBlock</code>,
     * <code>roundBlock</code>, or <code>truncateBlock</code> from Round.c
     * depending on the function parameter specified, and appends to the
     * given code buffer.
     * @param code the given buffer to append the code to.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block.
     */
    public void generateFireCode(StringBuffer code)
            throws IllegalActionException {
        ptolemy.actor.lib.conversions.Round actor = 
            (ptolemy.actor.lib.conversions.Round) getComponent();

        CodeStream tmpStream = new CodeStream(this);
        String function = actor.function.getExpression();
        String codeBlockName = (function.equals("ceil")) ? "ceilBlock" :
                              ((function.equals("floor")) ? "floorBlock" :
                              ((function.equals("round")) ? "roundBlock" :
                               "truncateBlock"));

        tmpStream.appendCodeBlock(codeBlockName);

        code.append(processCode(tmpStream.toString()));
    }

    /** 
     * Get the files needed by the code generated for the
     * Round actor.
     * @return A set of strings that are names of the files
     *  needed by the code generated for the Round actor.
     */
    public Set getIncludingFiles() {
        Set files = new HashSet();
        files.add("\"stdlib.h\"");
        files.add("\"limits.h\"");
        files.add("\"math.h\"");

        return files;
    }
}
