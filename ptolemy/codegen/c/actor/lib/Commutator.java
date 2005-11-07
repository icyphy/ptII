/* A code generation helper class for actor.lib.Commutator
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
 PROVIDED HEREUNDER IS ON AN \"AS IS\" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY

 */
package ptolemy.codegen.c.actor.lib;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import ptolemy.codegen.kernel.CCodeGeneratorHelper;
import ptolemy.kernel.util.IllegalActionException;

/**
 * A code generation helper class for ptolemy.actor.lib.Commutator.
 *
 * @author Man-Kit Leung
 * @version $Id$
 * @since Ptolemy II 5.1
 * @Pt.ProposedRating Red (mankit)
 * @Pt.AcceptedRating Red (mankit)
 */
public class Commutator extends CCodeGeneratorHelper {
    /**
     * Constructor method for the Commutator helper.
     * @param actor The associated actor.
     */
    public Commutator(ptolemy.actor.lib.Commutator actor) {
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
        StringBuffer code = new StringBuffer();
        code.append(super.generateFireCode());

        int portWidth = 
            ((ptolemy.actor.lib.Commutator) getComponent()).input.getWidth();

        ArrayList args = new ArrayList();
        args.add(new Integer(portWidth));
        
        for (int i = 0; i < portWidth; i++) {
            code.append(_generateBlockCode("fireBlock", args));        	
        }
        return code.toString();        
    }

    /**
     * Generate initialize code.
     * Read the <code>initBlock</code> from Commutator.c,
     * replace macros with their values and return the processed code string.
     * @return The processed code string.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    public String generateInitializeCode() throws IllegalActionException {
        super.generateInitializeCode();
        return _generateBlockCode("initBlock");
    }

    /**
     * Generate preinitialize code.
     * Reads the <code>preinitBlock</code> from Commutator.c,
     * replace macros with their values and return the processed code string.
     * @return The processed code string.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    public String generatePreinitializeCode() throws IllegalActionException {
        super.generatePreinitializeCode();
        return _generateBlockCode("preinitBlock");
    }

    /**
     * Generate wrap up code.
     * Read the <code>wrapupBlock</code> from Commutator.c,
     * replace macros with their values and append the processed code block
     * to the given code buffer.
     * @return The processed code string.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    public String generateWrapupCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        super.generateWrapupCode();
        code.append(_generateBlockCode("wrapupBlock"));
        return code.toString();
    }

    /**
     * Get the files needed by the code generated for the
     * Commutator actor.
     * @return A set of Strings that are names of the header files
     *  needed by the code generated for the Commutator actor.
     * @exception IllegalActionException Not Thrown in this subclass.
     */
    public Set getHeaderFiles() throws IllegalActionException {
        super.getHeaderFiles();

        Set files = new HashSet();
        files.add("<math.h>");

        return files;
    }
}
