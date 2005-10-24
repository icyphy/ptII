/* A helper class for actor.lib.Gaussian

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
package ptolemy.codegen.c.actor.lib;

import java.util.HashSet;
import java.util.Set;

import ptolemy.codegen.kernel.CCodeGeneratorHelper;
import ptolemy.kernel.util.IllegalActionException;

/**
 * A helper class for ptolemy.actor.lib.Gaussian.
 *
 * @author Man-Kit Leung
 * @version $Id$
 * @since Ptolemy II 5.1
 * @Pt.ProposedRating Yellow (mankit)
 * @Pt.AcceptedRating Yellow (mankit)
 */
public class Gaussian extends CCodeGeneratorHelper {
    /**
     * Constructor method for the Gaussian helper.
     * @param actor the associated actor
     */
    public Gaussian(ptolemy.actor.lib.Gaussian actor) {
        super(actor);
    }

    /**
     * Generate fire code.
     * Read the <code>fireBlock</code> from Gaussian.c, replace macros
     * with their values and append the processed code block to the given
     * code buffer.
     * @param code the given buffer to append the code to.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    public void generateFireCode(StringBuffer code)
            throws IllegalActionException {
        super.generateFireCode(code);
        code.append(_generateBlockCode("fireBlock"));
    }

    /**
     * Generate initialize code.
     * Parse the seed parameter of the actor. If the seed equals zero, then
     * append code that sets the seed variable to the sum of the current time
     * and the actor hashCode (This is what the original ptolemy actor does).
     * Otherwise, read the <code>setSeedBlock</code> from Gaussian.c,
     * replace macros with their values and return the processed code string.
     * @return The processed code string.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    public String generateInitializeCode() throws IllegalActionException {
        super.generateInitializeCode();

        ptolemy.actor.lib.Gaussian actor = (ptolemy.actor.lib.Gaussian) getComponent();

        _codeStream.clear();

        long seedValue;
        String seedString = actor.seed.getExpression();

        if (Character.isDigit(seedString.charAt(seedString.length() - 1))) {
            seedValue = Long.parseLong(seedString);
        } else {
            seedValue = Long.parseLong(seedString.substring(0, seedString
                    .length() - 1));
        }

        if (seedValue == 0) {
            _codeStream.append("$actorSymbol(seed) = time (NULL) + "
                    + actor.hashCode() + ";");
        } else {
            _codeStream.appendCodeBlock("setSeedBlock");
        }

        return processCode(_codeStream.toString());
    }

    /**
     * Generate preinitialize code.
     * Read the <code>preinitBlock</code> from Gaussian.c,
     * replace macros with their values and return the processed code string.
     * @return The processed code string.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    public String generatePreinitializeCode() throws IllegalActionException {
        super.generatePreinitializeCode();
        return processCode(_generateBlockCode("preinitBlock"));
    }

    /**
     * Generate shared code.
     * Reads the <code>sharedBlock</code> from Gaussian.c,
     * replace macros with their values and append the processed code
     * block to the given code buffer.
     * @return A set of strings that are code shared by multiple instances of
     *  the same actor.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    public Set generateSharedCode() throws IllegalActionException {
        // We don't need to process the code block here because the
        // sharedCode do not contain any macros.
        super.generateSharedCode();

        Set codeBlocks = new HashSet();
        codeBlocks.add(_generateBlockCode("sharedBlock", false));
        return codeBlocks;
    }

    /**
     * Get the files needed by the code generated for the
     * Gaussian actor.
     * @return A set of Strings that are names of the files
     *  needed by the code generated for the Gaussian actor.
     * @exception IllegalActionException Not Thrown in this subclass.
     */
    public Set getHeaderFiles() throws IllegalActionException {
        super.getHeaderFiles();

        Set files = new HashSet();
        files.add("<time.h>");
        files.add("<math.h>");
        return files;
    }
}
