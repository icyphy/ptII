/* Code generator helper class associated with the CaseDirector class.

 Copyright (c) 2005-2010 The Regents of the University of California.
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
import ptolemy.codegen.actor.Director;
import ptolemy.codegen.kernel.CodeGeneratorHelper;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// CaseDirector

/**
 Code generator helper class associated with the CaseDirector class.

 @author Gang Zhou
 @version $Id$
 @since Ptolemy II 5.2
 @Pt.ProposedRating Green (zgang)
 @Pt.AcceptedRating Green (cxh))
 */
public class CaseDirector extends Director {

    /** Construct the code generator helper associated with the given
     *  CaseDirector.
     *  @param director The associated ptolemy.actor.lib.hoc.CaseDirector
     */
    public CaseDirector(ptolemy.actor.lib.hoc.CaseDirector director) {
        super(director);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Generate the code for the firing of actors controlled by this
     *  director.
     *
     *  @return The generated fire code.
     *  @exception IllegalActionException If the helper associated with
     *   an actor throws it while generating fire code for the actor.
     */
    public String generateFireCode() throws IllegalActionException {

        StringBuffer code = new StringBuffer();

        ptolemy.actor.lib.hoc.Case container = (ptolemy.actor.lib.hoc.Case) getComponent()
                .getContainer();

        boolean useSwitch = false;
        if (container.control.getType() == BaseType.BOOLEAN
                || container.control.getType() == BaseType.INT) {
            // We have a boolean or integer, so we can use a C switch.
            useSwitch = true;
            code.append(_eol + "switch("
                    + _codeGenerator.generateVariableName(container.control)
                    + ") {" + _eol);
        }

        // If we are not using a C switch, save the default refinement and
        // output it last
        CompositeActor defaultRefinement = null;

        int refinementCount = 0;

        Iterator refinements = container.deepEntityList().iterator();
        while (refinements.hasNext()) {
            boolean fireRefinement = true;
            refinementCount++;
            CompositeActor refinement = (CompositeActor) refinements.next();
            CodeGeneratorHelper refinementHelper = (CodeGeneratorHelper) _getHelper(refinement);

            // FIXME: the refinement name may contain '$' signs.
            String refinementName = refinement.getName();

            if (!refinementName.equals("default")) {
                if (useSwitch) {
                    code.append("case " + refinementName + ":");
                } else {
                    if (refinementCount == 1) {
                        code.append("if (!strcmp(");
                    } else {
                        code.append("} else if (!strcmp(");
                    }

                    String controlVariable = _codeGenerator
                            .generateVariableName(container.control);

                    String controlType = codeGenType(container.control
                            .getType());

                    if (!controlType.equals("String")) {
                        controlVariable = "$convert_" + controlType
                                + "_String(" + controlVariable + ")";
                    }
                    code.append(controlVariable + ", \"" + refinementName
                            + "\")) {" + _eol);
                }
            } else {
                if (useSwitch) {
                    code.append("default: ");
                } else {
                    defaultRefinement = refinement;
                    // Skip Firing the default refinement for now,
                    // we'll do it later.
                    fireRefinement = false;
                }
            }

            // Fire the refinement
            if (fireRefinement) {
                code.append(refinementHelper.generateFireCode());
            }
            fireRefinement = true;

            if (useSwitch) {
                code.append("break;" + _eol + _eol);
            }
        }

        if (defaultRefinement != null) {
            code.append("} else {" + _eol);
            CodeGeneratorHelper defaultHelper = (CodeGeneratorHelper) _getHelper(defaultRefinement);
            code.append(defaultHelper.generateFireCode());
        }

        code.append("}" + _eol);

        return code.toString();
    }

}
