/* A helper class for ptolemy.actor.lib.Ramp
 Copyright (c) 1997-2005 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

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

import ptolemy.codegen.kernel.CCodeGeneratorHelper;
import ptolemy.data.type.Type;
import ptolemy.data.type.TypeLattice;
import ptolemy.graph.CPO;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// Ramp

/**
 A helper class for ptolemy.actor.lib.Ramp.

 @author Gang Zhou
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (eal)
 */
public class Ramp extends CCodeGeneratorHelper {
    /** Constructor method for the Ramp helper.
     *  @param actor the associated actor
     */
    public Ramp(ptolemy.actor.lib.Ramp actor) {
        super(actor);
    }

    /** Generate fire code.
     *  The method reads in <code>fireBlock</code> from Ramp.c,
     *  replaces macros with their values and appends the processed code
     *  block to the given code buffer.
     * @return The generated code.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    public String generateFireCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(super.generateFireCode());
        code.append(_generateBlockCode("fireBlock"));
        return code.toString();
    }

    /** Generate the initialize code.
     *  @return The initialize code.
     *  @exception IllegalActionException
     */
    public String generateInitializeCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(super.generateInitializeCode());
        code.append(_generateBlockCode("initBlock"));
        return processCode(code.toString());
    }

    /** Generate the preinitialize code. Declare the variable state.
     *  @return The preinitialize code.
     *  @exception IllegalActionException
     */
    public String generatePreinitializeCode() throws IllegalActionException {
        // FIXME: so far the code only works for primitive types.
        StringBuffer code = new StringBuffer();
        code.append(super.generatePreinitializeCode());

        ptolemy.actor.lib.Ramp actor = (ptolemy.actor.lib.Ramp) getComponent();
        Type initType = actor.init.getType();
        Type stepType = actor.step.getType();
        int comparison = TypeLattice.compare(initType, stepType);

        if ((comparison == CPO.HIGHER) || (comparison == CPO.SAME)) {
            code.append(initType.toString() + " $actorSymbol(state);\n");
        } else if (comparison == CPO.LOWER) {
            code.append(stepType.toString() + " $actorSymbol(state);\n");
        } else {
            throw new IllegalActionException(actor, "type incomparable.");
        }

        return processCode(code.toString());
    }
}
