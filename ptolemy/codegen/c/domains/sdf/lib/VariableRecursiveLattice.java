/* A code generation helper class for domains.sdf.lib.VariableRecursiveLattice

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
package ptolemy.codegen.c.domains.sdf.lib;

import java.util.HashSet;
import java.util.Set;

import ptolemy.codegen.c.actor.lib.CodeStream;
import ptolemy.codegen.kernel.CCodeGeneratorHelper;
import ptolemy.kernel.util.IllegalActionException;

/**
 * A code generation helper class for ptolemy.domains.sdf.lib.VariableRecursiveLattice.
 *
 * @author Man-Kit Leung
 * @version $Id$
 * @since Ptolemy II 5.1
 * @Pt.ProposedRating Red (mankit)
 * @Pt.AcceptedRating Red (mankit)
 */
public class VariableRecursiveLattice extends CCodeGeneratorHelper {

    /**
     * Constructor method for the VariableRecursiveLattice helper.
     * @param actor The associated actor.
     */
    public VariableRecursiveLattice(ptolemy.domains.sdf.lib.VariableRecursiveLattice actor) {
        super(actor);
    }

    /**
     * Generate fire code.
     * The method reads in <code>fireBlock</code> from VariableRecursiveLattice.c,
     * replaces macros with their values and appends the processed code
     * block to the given code buffer.
     * @param code the given buffer to append the code to.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    public void  generateFireCode(StringBuffer code)
        throws IllegalActionException {
        ptolemy.domains.sdf.lib.VariableRecursiveLattice actor =
            (ptolemy.domains.sdf.lib.VariableRecursiveLattice) getComponent();
        CodeStream _codeStream = new CodeStream(this);
        _codeStream.appendCodeBlock("fireBlock");

        code.append(processCode(_codeStream.toString()));
    }

    /**
     * Generate initialize code.
     * This method reads the <code>initBlock</code> from VariableRecursiveLattice.c,
     * replaces macros with their values and returns the processed code string.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     * @return The processed code string.
     */
    public String generateInitializeCode()
        throws IllegalActionException {
        super.generateInitializeCode();
        CodeStream _codeStream = new CodeStream(this);
        _codeStream.appendCodeBlock("initBlock");

        return processCode(_codeStream.toString());
    }

    /**
     * Generate preinitialize code.
     * This method reads the <code>preinitBlock</code> from VariableRecursiveLattice.c,
     * replaces macros with their values and returns the processed code string.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     * @return The processed code string.
     */
    public String generatePreinitializeCode()
        throws IllegalActionException {
        super.generatePreinitializeCode();
        CodeStream _codeStream = new CodeStream(this);
        _codeStream.appendCodeBlock("preinitBlock");

        return processCode(_codeStream.toString());
    }

    /**
     * Generate wrap up code.
     * This method reads the <code>wrapupBlock</code>
     * from VariableRecursiveLattice.c,
     * replaces macros with their values and appends the processed code block
     * to the given code buffer.
     * @param code the given buffer to append the code to.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    public void generateWrapupCode(StringBuffer code)
        throws IllegalActionException {
        ptolemy.domains.sdf.lib.VariableRecursiveLattice actor =
            (ptolemy.domains.sdf.lib.VariableRecursiveLattice) getComponent();
        CodeStream _codeStream = new CodeStream(this);
        _codeStream.appendCodeBlock("wrapupBlock");

        code.append(processCode(_codeStream.toString()));
    }

    /**
     * Get the files needed by the code generated for the
     * VariableRecursiveLattice actor.
     * @return A set of strings that are names of the header files
     *  needed by the code generated for the VariableRecursiveLattice actor.
     */
    public Set getHeaderFiles() {
        Set files = new HashSet();
        files.add("\"math.h\"");

        return files;
    }
}
