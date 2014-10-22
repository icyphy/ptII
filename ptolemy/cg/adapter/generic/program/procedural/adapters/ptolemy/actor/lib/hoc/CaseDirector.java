/* Code generator adapter class associated with the CaseDirector class.

 Copyright (c) 2005-2014 The Regents of the University of California.
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
package ptolemy.cg.adapter.generic.program.procedural.adapters.ptolemy.actor.lib.hoc;

import static ptolemy.cg.kernel.generic.GenericCodeGenerator.INDENT2;

import java.util.Iterator;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.cg.adapter.generic.adapters.ptolemy.actor.Director;
import ptolemy.cg.kernel.generic.program.NamedProgramCodeGeneratorAdapter;
import ptolemy.cg.kernel.generic.program.ProgramCodeGenerator;
import ptolemy.data.BooleanToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// CaseDirector

/**
 Code generator adapter class associated with the CaseDirector class.

 @author Gang Zhou
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Green (zgang)
 @Pt.AcceptedRating Green (cxh))
 */
public class CaseDirector extends Director {

    /** Construct the code generator adapterassociated with the given
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
     *  @exception IllegalActionException If the adapter associated with
     *   an actor throws it while generating fire code for the actor.
     */
    @Override
    public String generateFireCode() throws IllegalActionException {
        //new Exception("CaseDirector.generateFireCode()").printStackTrace();
        StringBuffer code = new StringBuffer();

        ProgramCodeGenerator codeGenerator = getCodeGenerator();
        ((BooleanToken) codeGenerator.inline.getToken()).booleanValue();

        ptolemy.actor.lib.hoc.Case container = (ptolemy.actor.lib.hoc.Case) getComponent()
                .getContainer();

        boolean useIf = false;
        boolean useSwitch = false;
        if (container.control.getType() == BaseType.BOOLEAN) {
            useIf = true;
            code.append(_eol + INDENT2 + " if ("
                    + codeGenerator.generateVariableName(container.control)
                    //+ ((NamedProgramCodeGeneratorAdapter)codeGenerator.getAdapter(container)).processCode("$get(" + container.control.getPort().getName() + ")")
                    + ") {" + _eol);
        } else if (container.control.getType() == BaseType.INT) {
            // We have a boolean or integer, so we can use a C switch.
            useSwitch = true;
            code.append(_eol + INDENT2 + "switch("
                    + codeGenerator.generateVariableName(container.control)
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
            NamedProgramCodeGeneratorAdapter refinementAdapter = (NamedProgramCodeGeneratorAdapter) codeGenerator
                    .getAdapter(refinement);

            // FIXME: the refinement name may contain '$' signs.
            String refinementName = refinement.getName();

            if (!refinementName.equals("default")) {
                if (useIf) {
                    // Noop
                } else if (useSwitch) {
                    code.append(INDENT2 + "case " + refinementName + ":");
                } else {
                    if (refinementCount == 1) {
                        // Add String to the list of _newTypesUsed.
                        //refinementAdapter.addNewTypeUsed("String");
                        code.append(INDENT2 + "if (!strcmp(");
                    } else {
                        code.append(INDENT2 + "} else if (!strcmp(");
                    }
                    code.append(codeGenerator
                            .generateVariableName(container.control)
                            + ".payload.String, "
                            + "\""
                            + refinementName
                            + "\")) {" + _eol);
                }
            } else {
                if (useIf) {
                    code.append(INDENT2 + "} else {" + _eol);
                } else if (useSwitch) {
                    code.append(INDENT2 + "default: ");
                } else {
                    defaultRefinement = refinement;
                    // Skip Firing the default refinement for now,
                    // we'll do it later.
                    fireRefinement = false;
                }
            }

            // Fire the refinement
            if (fireRefinement) {
                code.append(refinementAdapter.generateFireCode());
            }
            fireRefinement = true;

            if (useSwitch) {
                code.append(INDENT2 + "break;" + _eol + _eol);
            }
        }

        if (defaultRefinement != null) {
            code.append(INDENT2 + "} else {" + _eol);
            NamedProgramCodeGeneratorAdapter defaultAdapter = (NamedProgramCodeGeneratorAdapter) codeGenerator
                    .getAdapter(defaultRefinement);
            code.append(defaultAdapter.generateFireCode());
        }

        code.append(INDENT2 + "}" + _eol);

        return code.toString();
    }

    /** Generate code for transferring enough tokens to complete an internal
     *  iteration.
     *  @param inputPort The port to transfer tokens.
     *  @param code The string buffer that the generated code is appended to.
     *  @exception IllegalActionException If thrown while transferring tokens.
     */
    @Override
    final public void generateTransferInputsCode(IOPort inputPort,
            StringBuffer code) throws IllegalActionException {
        generateTransferInputsCode(inputPort, code, true);
    }

    /** Generate code for transferring enough tokens to fulfill the output
     *  production rate.
     *  @param outputPort The port to transfer tokens.
     *  @param code The string buffer that the generated code is appended to.
     *  @exception IllegalActionException If thrown while transferring tokens.
     */
    @Override
    public void generateTransferOutputsCode(IOPort outputPort, StringBuffer code)
            throws IllegalActionException {
        generateTransferOutputsCode(outputPort, code, true);
    }

    //     /** Generate code for transferring enough tokens to complete an internal
    //      *  iteration.
    //      *  @param inputPort The port to transfer tokens.
    //      *  @param code The string buffer that the generated code is appended to.
    //      *  @exception IllegalActionException If thrown while transferring tokens.
    //      */
    //     public void generateTransferInputsCode(IOPort inputPort, StringBuffer code)
    //             throws IllegalActionException {
    //         code.append(CodeStream.indent(getCodeGenerator().comment(
    //                 "Transfer tokens to the inside")));

    //         NamedProgramCodeGeneratorAdapter _compositeActorAdapter = (NamedProgramCodeGeneratorAdapter) getCodeGenerator()
    //                 .getAdapter(_director.getContainer());

    //         for (int i = 0; i < inputPort.getWidth(); i++) {
    //             if (i < inputPort.getWidthInside()) {
    //                 String name = inputPort.getName();

    //                 if (inputPort.isMultiport()) {
    //                     name = name + '#' + i;
    //                 }

    //                 //FIXME: What should be the director? Executive or local?
    //                 code.append(CodeStream.indent(_compositeActorAdapter
    //                                 .getReference("@" + name, false, true)));
    //                 code.append(" = ");
    //                 code.append(_compositeActorAdapter.getReference(name, false, true));
    //                 code.append(";" + _eol);
    //             }
    //         }

    //         // Generate the type conversion code before fire code.
    //         code.append(_compositeActorAdapter.generateTypeConvertFireCode(true));
    //     }

    //     /** Generate code for transferring enough tokens to fulfill the output
    //      *  production rate.
    //      *  @param outputPort The port to transfer tokens.
    //      *  @param code The string buffer that the generated code is appended to.
    //      *  @exception IllegalActionException If thrown while transferring tokens.
    //      */
    //     public void generateTransferOutputsCode(IOPort outputPort, StringBuffer code)
    //             throws IllegalActionException {
    //         // FIXME: This is like Director.generatePortName(),
    //         // except the left hand side reference is obtained from the container.

    //         code.append(getCodeGenerator()
    //                 .comment("Case Director Transfer tokens to the outside"));

    //         NamedProgramCodeGeneratorAdapter _compositeActorAdapter = (NamedProgramCodeGeneratorAdapter) getCodeGenerator()
    //                 .getAdapter(_director.getContainer());

    //         for (int i = 0; i < outputPort.getWidthInside(); i++) {
    //             if (i < outputPort.getWidth()) {
    //                 String name = outputPort.getName();

    //                 if (outputPort.isMultiport()) {
    //                     name = name + '#' + i;
    //                 }
    //                 System.out.println("-->CaseDirector.generateTransferOutputsCode(" + outputPort +",\n<<<<" + code + "\n>>>>");
    //                 // Get the references from the Executive Director.
    //                 code.append(_compositeActorAdapter.getReference(name, false, true)
    //                         + " = ");
    //                 code.append(_compositeActorAdapter.getReference("@" + name,
    //                                 false, true));
    //                 code.append(";" + _eol);
    //             }
    //         }
    //     }

}
