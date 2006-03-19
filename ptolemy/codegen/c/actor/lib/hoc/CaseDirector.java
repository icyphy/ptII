/* Code generator helper class associated with the CaseDirector class.

 Copyright (c) 2005-2006 The Regents of the University of California.
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
package ptolemy.codegen.c.actor.lib.hoc;

import java.util.Iterator;

import ptolemy.actor.CompositeActor;
import ptolemy.codegen.kernel.CodeGeneratorHelper;
import ptolemy.codegen.kernel.Director;
import ptolemy.data.BooleanToken;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// CaseDirector

/**
 Code generator helper class associated with the CaseDirector class.

 @author Gang Zhou
 @version $Id$
 @since Ptolemy II 5.2
 @Pt.ProposedRating Red (zgang)
 @Pt.AcceptedRating Red (zgang)
 */
public class CaseDirector extends Director {

    /** Construct the code generator helper associated with the given
     *  CaseDirector.
     *  @param director The associated ptolemy.actor.lib.hoc.CaseDirector
     */
    public CaseDirector(ptolemy.actor.lib.hoc.CaseDirector director) {
        super(director);
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////

    /** Generate the code for the firing of actors controlled by this
     *  director.  
     *  
     *  @return The generated fire code.
     *  @exception IllegalActionException If the helper associated with
     *   an actor throws it while generating fire code for the actor.
     */
    public String generateFireCode() throws IllegalActionException {

        StringBuffer code = new StringBuffer();

        boolean inline = ((BooleanToken) _codeGenerator.inline.getToken())
                .booleanValue();

        ptolemy.actor.lib.hoc.Case container = (ptolemy.actor.lib.hoc.Case) getComponent()
                .getContainer();

        code.append("switch ("
                + CodeGeneratorHelper.generateVariableName(container.control)
                + ") {\n");

        Iterator refinements = container.deepEntityList().iterator();
        while (refinements.hasNext()) {
            CompositeActor refinement = (CompositeActor) refinements.next();
            String refinementName = refinement.getName();
            if (!refinementName.equals("default")) {
                code.append("case ");
            }
            code.append(refinementName + ":\n");

            CodeGeneratorHelper refinementHelper = (CodeGeneratorHelper) _getHelper(refinement);

            // fire the refinement
            if (inline) {
                code.append(refinementHelper.generateFireCode());
                code.append(refinementHelper.generateTypeConvertFireCode());
            } else {
                code.append(CodeGeneratorHelper.generateName(refinement)
                        + "();\n");
            }

            code.append("break;\n");
        }

        code.append("}");

        return code.toString();
    }

}
