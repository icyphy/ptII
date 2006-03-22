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

import ptolemy.codegen.c.kernel.CCodeGeneratorHelper;
import ptolemy.kernel.util.IllegalActionException;

/**
 * A code generation helper class for
 * ptolemy.domains.sdf.lib.VariableRecursiveLattice.
 *
 * @author Man-Kit Leung
 * @version $Id$
 * @since Ptolemy II 5.1
 * @Pt.ProposedRating Yellow (cxh)
 * @Pt.AcceptedRating Red (mankit)
 */
public class VariableRecursiveLattice extends CCodeGeneratorHelper {
    /**
     * Constructor method for the VariableRecursiveLattice helper.
     * @param actor The associated actor.
     */
    public VariableRecursiveLattice(
            ptolemy.domains.sdf.lib.VariableRecursiveLattice actor) {
        super(actor);
    }

    /**
     * Get the files needed by the code generated for the
     * VariableRecursiveLattice actor.
     * @return A set of strings that are names of the header files
     *  needed by the code generated for the VariableRecursiveLattice actor.
     * @exception IllegalActionException Not Thrown in this subclass.
     */
    public Set getHeaderFiles() throws IllegalActionException {
        Set files = new HashSet();
        files.add("\"math.h\"");
        return files;
    }
}
