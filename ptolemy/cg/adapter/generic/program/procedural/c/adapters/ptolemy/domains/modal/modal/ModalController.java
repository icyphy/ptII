/* Code generator helper for modal controller.

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
package ptolemy.cg.adapter.generic.program.procedural.c.adapters.ptolemy.domains.modal.modal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ptolemy.actor.Actor;
import ptolemy.actor.TypedIOPort;
import ptolemy.cg.adapter.generic.program.procedural.adapters.ptolemy.domains.modal.kernel.FSMActor.TransitionRetriever;
import ptolemy.cg.adapter.generic.program.procedural.c.adapters.ptolemy.domains.modal.kernel.FSMActor;
import ptolemy.cg.kernel.generic.CodeGeneratorAdapter;
import ptolemy.cg.kernel.generic.program.NamedProgramCodeGeneratorAdapter;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.modal.kernel.State;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// ModalController

/**
 Code generator helper for modal controller.

 @author William Lucas
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Red (wlc)
 @Pt.AcceptedRating Red (wlc)
 */
public class ModalController
extends
ptolemy.cg.adapter.generic.program.procedural.adapters.ptolemy.domains.modal.modal.ModalController {

    /** Construct the code generator helper associated
     *  with the given modal controller.
     *  @param component The associated component.
     */
    public ModalController(ptolemy.domains.modal.modal.ModalController component) {
        super(component);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         Public methods                    ////

    /** Generate the fire code of the associated controller.
     *  All the steps are described below
     *  It is slightly different from the super class in order to take
     *  into account the receivers (if embedded in a DE model)
     *
     *  @return The fire code of the associated controller.
     *  @exception IllegalActionException If the adapter associated with
     *   an actor throws it while generating fire code for the actor
     */
    @Override
    public String generateFireCode() throws IllegalActionException {
        FSMActor controllerHelper;
        try {
            controllerHelper = new FSMActor(_myController);
            controllerHelper.setCodeGenerator(getCodeGenerator());
            controllerHelper.setTemplateParser(getTemplateParser());
        } catch (NameDuplicationException ndx) {
            throw new IllegalActionException(ndx.toString());
        }
        StringBuffer code = new StringBuffer();

        String name = _myController.getFullName().substring(1);
        String modalName = name.replace("_Controller", "");
        modalName = modalName.replace('.', '_');

        // Generate code for preemptive transition.
        code.append(_eol
                + getCodeGenerator().comment("1. Preemptive Transition"));

        controllerHelper.generateTransitionCode(code,
                new PreemptiveTransitions());

        code.append(_eol);

        // Check to see if a preemptive transition is taken.
        code.append("if (" + modalName + "__transitionFlag == 0) {" + _eol);

        // Generate code for refinements.
        if (_generateRefinementCode(code)) {
            // Transfer the outputs from the refinement to modal model
            code.append(_eol
                    + "director->directorTransferModalOutputs(director);"
                    + _eol);
        } else {
            code.append(_eol
                    + "PblMap * outputMap = pblMapNewHashMap();" + _eol
                    + "director->transferModalOutputs(outputMap);" + _eol
                    + "pblMapFree(outputMap);" + _eol);
        }

        // Generate code for non-preemptive transition
        code.append(getCodeGenerator().comment("2. Nonpreemptive Transition"));

        // generateTransitionCode(code);
        controllerHelper.generateTransitionCode(code,
                new NonPreemptiveTransitions());
        code.append("}" + _eol);

        // Transfer the outputs from inside to outside of the modal model
        code.append(_eol + "director->transferOutputs(director);" + _eol);

        return code.toString();

    }

    /** Generate the postfire code of the associated controller.
     * We generate a switch because we only need to call the postfire method
     * of the current state.
     *
     *  @return The postfire code of the associated controller.
     *  @exception IllegalActionException If the adapter associated with
     *   an actor throws it while generating postfire code for the actor
     */
    @Override
    public String generatePostfireCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        String name = _myController.getFullName().substring(1);
        name = name.replace('.', '_');

        code.append(_eol + "switch (" + name + "__currentState) {");
        Iterator states = _myController.entityList().iterator();
        while (states.hasNext()) {
            State state = (State) states.next();
            String stateName = CodeGeneratorAdapter.generateName(state);
            code.append(_eol + "case " + stateName + " : {");
            Actor[] actors = state.getRefinement();

            if (actors != null) {
                for (Actor actor : actors) {
                    NamedProgramCodeGeneratorAdapter actorHelper = (NamedProgramCodeGeneratorAdapter) getCodeGenerator()
                            .getAdapter(actor);
                    code.append(_eol + actorHelper.generatePostfireCode());
                }
            }

            code.append(_eol + "}" + _eol + "break;");
        }
        code.append(_eol + "}" + _eol + "return true;");

        return code.toString();
    }

    @Override
    public String generateVariableDeclaration() throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        List<TypedIOPort> inputPorts = _myController.inputPortList();
        List<TypedIOPort> outputPorts = _myController.outputPortList();
        String name = _myController.getFullName().substring(1);
        String modalName = name.replace("_Controller", "");
        name = name.replace('.', '_');
        modalName = modalName.replace('.', '_');
        TypedIOPort inputPort, outputPort;
        code.append(_eol
                + getCodeGenerator().comment(
                        "Beginning of create controller variables."));
        for (int i = 0; i < inputPorts.size(); i++) {

            inputPort = inputPorts.get(i);
            if (!outputPorts.contains(inputPort)) {
                int width = inputPort.getWidth();

                code.append(inputPort.getType() + " " + name + "_"
                        + inputPort.getName());
                if (width > 1) {
                    code.append("[" + width + "]");
                }
                code.append(";" + _eol);
                code.append("bool " + name + "_" + inputPort.getName()
                        + "_isPresent;" + _eol);
            }
        }
        for (int i = 0; i < outputPorts.size(); i++) {

            outputPort = outputPorts.get(i);
            int width = outputPort.getWidth();
            code.append(outputPort.getType() + " " + name + "_"
                    + outputPort.getName());
            if (width > 1) {
                code.append("[" + width + "]");
            }
            code.append(";" + _eol);
        }

        //code.append("int " + name + "__currentState;" + _eol);
        code.append("int " + modalName + "__transitionFlag;" + _eol);
        StringBuffer enumStates = new StringBuffer(_eol + "enum " + name
                + "__currentState {");

        Iterator states = _myController.entityList().iterator();
        boolean first = true;
        while (states.hasNext()) {
            if (first) {
                first = false;
            } else {
                enumStates.append(", ");
            }
            State state = (State) states.next();
            String stateName = CodeGeneratorAdapter.generateName(state);
            enumStates.append(stateName);
        }
        enumStates.append("};");
        code.append(enumStates);
        code.append(_eol + "enum " + name + "__currentState " + name
                + "__currentState;");

        List<?> list = _myController.getModifiedVariables();
        List<Parameter> done = new ArrayList();
        for (Object o : list) {
            if (o instanceof Parameter && !done.contains(o)) {
                Parameter parameter = (Parameter) o;
                getCodeGenerator().addModifiedVariables(parameter);
                done.add(parameter);
                code.append(_eol + parameter.getType() + " "
                        + getCodeGenerator().generateVariableName(parameter)
                        + " = " + parameter.getValueAsString() + ";" + _eol);
            }
        }

        code.append(_eol
                + getCodeGenerator().comment(
                        "End of create controller variables"));
        return code.toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                      Protected methods                    ////

    /** Generate code for the firing of refinements.
     *
     *  @param code The string buffer that the generated code is appended to.
     *  @return true if any of the states had refinements.
     *  @exception IllegalActionException If the helper associated with
     *   an actor throws it while generating fire code for the actor.
     */
    @Override
    protected boolean _generateRefinementCode(StringBuffer code)
            throws IllegalActionException {

        boolean refined = false;
        String name = _myController.getFullName().substring(1);
        name = name.replace('.', '_');

        int depth = 1;
        code.append(_getIndentPrefix(depth));
        code.append("switch (" + name + "__currentState) {" + _eol);

        Iterator states = _myController.entityList().iterator();
        depth++;

        while (states.hasNext()) {
            code.append(_getIndentPrefix(depth));

            depth++;

            State state = (State) states.next();
            code.append("case " + CodeGeneratorAdapter.generateName(state)
                    + ":" + _eol);

            Actor[] actors = state.getRefinement();

            if (actors != null) {
                refined = true;
                for (Actor actor : actors) {
                    //                    NamedProgramCodeGeneratorAdapter actorHelper = (NamedProgramCodeGeneratorAdapter) getCodeGenerator()
                    //                            .getAdapter(actor);
                    // fire the actor
                    String actorName = generateName((NamedObj) actor);
                    code.append(_eol + actorName + "->iterate(" + actorName
                            + ", 1);" + _eol);
                }
            }

            code.append(_getIndentPrefix(depth));

            code.append("break;" + _eol); //end of case statement
            depth--;
        }
        depth--;
        code.append(_getIndentPrefix(depth));
        code.append("}" + _eol); //end of switch statement

        return refined;

    }

    ///////////////////////////////////////////////////////////////////
    ////                      Inner classes                        ////

    /** Retrieve the non-preemptive transitions. */
    private static class NonPreemptiveTransitions implements
    TransitionRetriever {
        /** Retrieve the non-preemptive transitions.
         *  @param state The state
         *  @return An iterator that refers to the non-preemptive transitions.
         */
        @Override
        public Iterator retrieveTransitions(State state) {
            try {
                return state.nonpreemptiveTransitionList().iterator();
            } catch (IllegalActionException e) {
                throw new InternalErrorException(state, e,
                        "Error evaluating transition parameters.");
            }
        }
    }

    /** An inner class which retrieves the preemptive transitions. */
    private static class PreemptiveTransitions implements TransitionRetriever {
        /** Retrieve the preemptive transitions.
         *  @param state The state
         *  @return An iterator that refers to the preemptive transitions.
         */
        @Override
        public Iterator retrieveTransitions(State state) {
            try {
                return state.preemptiveTransitionList().iterator();
            } catch (IllegalActionException e) {
                throw new InternalErrorException(state, e,
                        "Error evaluating transition parameters.");
            }
        }
    }
}
