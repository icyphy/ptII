/* Code generator helper for FSMActor

 Copyright (c) 2013-2014 The Regents of the University of California.
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
package ptolemy.cg.adapter.generic.program.procedural.c.adapters.ptolemy.domains.modal.kernel;

import java.util.Iterator;

import ptolemy.actor.Actor;
import ptolemy.actor.TypedIOPort;
import ptolemy.cg.kernel.generic.program.CodeStream;
import ptolemy.domains.modal.kernel.State;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// FSMActor

/**
 Code generator adapter for FSMActor.

 @author William Lucas
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class FSMActor
extends
ptolemy.cg.adapter.generic.program.procedural.adapters.ptolemy.domains.modal.kernel.FSMActor {

    /** Construct the code generator adapter associated
     *  with the given FSMActor.
     *  @param component The associated component.
     *  @exception IllegalActionException If thrown by the parent
     *  class.
     *  @exception NameDuplicationException If thrown by the parent
     *  class.
     */
    public FSMActor(ptolemy.domains.modal.kernel.FSMActor component)
            throws NameDuplicationException, IllegalActionException {
        super(component);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Generate the initialize code of the associated FSMActor. It
     *  generates code for initializing current state with initial
     *  state, and initializing current configuration of the container
     *  when it applies (i.e., when this FSMActor works as a modal
     *  controller for a MultirateFSMDirector).
     *
     *  @return The initialize code of the associated FSMActor.
     *  @exception IllegalActionException If initial state cannot be found,
     *   configuration number cannot be updated or code cannot be processed.
     */
    @Override
    public String generateInitializeCode() throws IllegalActionException {
        StringBuffer codeBuffer = new StringBuffer();
        codeBuffer.append(super.generateInitializeCode());

        //        ptolemy.domains.modal.kernel.FSMActor fsmActor = (ptolemy.domains.modal.kernel.FSMActor) getComponent();
        //        State initialState = fsmActor.getInitialState();
        //
        //        _updateCurrentState(codeBuffer, initialState);

        return processCode(codeBuffer.toString());
    }

    /** Generate the preinitialize code of the associated FSMActor. It
     *  declares two variables for this actor: currentState and transitionFlag.
     *  currentState is an int representing this actor's current state.
     *  transitionFlag is an unsigned char to indicate if a preemptive
     *  transition is taken. It also defines a symbolic constant to each
     *  state.
     *  @return The preinitialize code of the associated FSMActor.
     *  @exception IllegalActionException If thrown when creating buffer
     *   size and offset map or processing code.
     */
    @Override
    public String generatePreinitializeCode() throws IllegalActionException {
        super.generatePreinitializeCode();

        //        ptolemy.domains.modal.kernel.FSMActor fsmActor = (ptolemy.domains.modal.kernel.FSMActor) getComponent();

        CodeStream codeStream = _templateParser.getCodeStream();
        //         ArrayList args = new ArrayList(2);
        //         int index = 0;
        //         args.add("");
        //         args.add("");
        //         for (State state : (List<State>) fsmActor.entityList()) {
        //             args.set(0, _generateStateConstantLabel(state));
        //             args.set(1, Integer.toString(index++));
        //             codeStream.appendCodeBlock("defineState", args);
        //         }

        // Add input ports
        Iterator inputPorts = ((Actor) getComponent()).inputPortList()
                .iterator();

        while (inputPorts.hasNext()) {
            TypedIOPort inputPort = (TypedIOPort) inputPorts.next();

            codeStream.append(_eol + inputPort.getType() + " "
                    + generateName(inputPort));
            if (inputPort.isMultiport()) {
                codeStream.append("[" + inputPort.getWidth() + "]");
            }
            codeStream.append(";");
        }
        return processCode(codeStream.toString());
    }

    //     /** Generate code for making transition. It generates code for both choice
    //      *  action and commit action.
    //      *
    //      *  @param code The string buffer that the generated code is appended to.
    //      *  @param transitionRetriever An instance of a class implementing
    //      *  a method.
    //      *   which returns an iterator of all, preemptive or non-preemptive
    //      *   transitions of the current state.
    //      *  @exception IllegalActionException If thrown while generating
    //      *  transition code.
    //      */
    //     @Override
    //     public void generateTransitionCode(StringBuffer code,
    //             TransitionRetriever transitionRetriever)
    //             throws IllegalActionException {
    //         StringBuffer codeBuffer = new StringBuffer();
    //         codeBuffer.append(getCodeGenerator().comment(
    //                 "Generate Transition Code -c-."));

    //         ptolemy.domains.modal.kernel.FSMActor fsmActor = (ptolemy.domains.modal.kernel.FSMActor) getComponent();

    //         String name = fsmActor.getFullName().substring(1);
    //         String modalName = name.replace("_Controller", "");
    //         name = name.replace('.', '_').replace(' ', '_');
    //         modalName = modalName.replace('.', '_').replace(' ', '_');

    //         // The default value 1 of transitionFlag means the transition
    //         // will be taken. If no transition is actually taken, it will be
    //         // set to value 0.
    //         codeBuffer.append(_eol + modalName + "__transitionFlag = 1;" + _eol);

    //         code.append(getCodeGenerator().comment("generateTransitionCode(): generating ports"));
    //         Iterator inputPorts = ((Actor) getComponent()).inputPortList()
    //                 .iterator();
    //         // add input ports
    //         while (inputPorts.hasNext()) {
    //             if (getComponent() instanceof ModalController) {
    //                 break;
    //             }
    //             TypedIOPort inputPort = (TypedIOPort) inputPorts.next();
    //             for (int i = 0; i < inputPort.getWidth(); i++) {
    //                 codeBuffer.append(_eol + "if ($hasToken("
    //                         + generateSimpleName(inputPort));
    //                 if (inputPort.isMultiport()) {
    //                     codeBuffer.append("#" + i);
    //                 }
    //                 codeBuffer.append(")) {" + _eol);

    //                 codeBuffer.append(_eol + generateName(inputPort));
    //                 if (inputPort.isMultiport()) {
    //                     // FIXME: Probably a better way to handle channels.
    //                     codeBuffer.append("[" + i + "]");
    //                 }
    //                 codeBuffer.append(" = $get("
    //                         + generateSimpleName(inputPort));
    //                 if (inputPort.isMultiport()) {
    //                     codeBuffer.append("#" + i);
    //                 }
    //                 codeBuffer.append(");" + _eol);
    //                 codeBuffer.append("}" + _eol);
    //             }
    //         }
    //         code.append(getCodeGenerator().comment("generateTransitionCode(): done generating ports"));

    //         // States are numbered according to the order they are created,
    //         // i.e., the same order as in list returned by the method entityList().
    //         codeBuffer.append("switch (" + name + "__currentState" + ")" + _eol
    //                 + "{" + _eol);

    //         for (State state : (List<State>) fsmActor.entityList()) {
    //             // For each state...
    //             codeBuffer.append("case "
    //                     + CodeGeneratorAdapter.generateName(state) + ":" + _eol);

    //             // The transitions (all, preemptive or non-preemptive
    //             // depending on the instance of TransitionRetriever given)
    //             // that need to be tried.
    //             Iterator transitions = transitionRetriever
    //                     .retrieveTransitions(state);
    //             // Reorder transitions so that default transitions are at
    //             // the end of the list.
    //             List reOrderedTransitions = new LinkedList();
    //             List defaultTransitions = new LinkedList();
    //             while (transitions.hasNext()) {
    //                 Transition transition = (Transition) transitions.next();
    //                 if (generateSimpleName(transition).equals("default")) {
    //                     defaultTransitions.add(transition);
    //                 } else {
    //                     reOrderedTransitions.add(transition);
    //                 }
    //             }
    //             reOrderedTransitions.addAll(defaultTransitions);
    //             transitions = reOrderedTransitions.iterator();

    //             int transitionCount = 0;

    //             boolean hasDefaultCase = false;
    //             while (!hasDefaultCase && transitions.hasNext()) {

    //                 Transition transition = (Transition) transitions.next();

    //                 String guard = transition.getGuardExpression();

    //                 if (transition.isDefault()
    //                         || guard.toLowerCase(Locale.getDefault())
    //                                 .equals("true")) {

    //                     // We don't need to generate if-predicate for this,
    //                     // and we can skip the rest of the transitions.
    //                     // FIXME: Need to handle nondeterministic transitions.
    //                     hasDefaultCase = true;

    //                 } else {
    //                     // generate code for guard expression
    //                     if (transitionCount == 0) {
    //                         codeBuffer.append("if (");
    //                     } else {
    //                         codeBuffer.append("else if (");
    //                     }
    //                     transitionCount++;

    //                     if (guard.compareTo("") == 0) {
    //                         codeBuffer.append("true) ");
    //                     } else {

    //                         PtParser parser = new PtParser();

    //                         //int index = guard.indexOf("==");
    //                         ASTPtRootNode guardParseTree = parser
    //                                 .generateParseTree(guard);

    //                         if (getTemplateParser() == null) {
    //                             if (getCodeGenerator() == null) {
    //                                 // The code generator was not being found.
    //                                 // $PTII/bin/ptcg -language java $PTII/ptolemy/cg/adapter/generic/program/procedural/java/adapters/ptolemy/domains/modal/test/auto/Simple01.xml
    //                                 throw new InternalErrorException(
    //                                         this,
    //                                         null,
    //                                         "Can't find a code generator?, be sure to call setCodeGenerator() after instantiating FSMActor.");
    //                             }
    //                             getCodeGenerator().getAdapter(fsmActor);
    //                         }

    //                         ParseTreeCodeGenerator parseTreeCodeGenerator = getTemplateParser()
    //                                 .getParseTreeCodeGenerator();

    //                         parseTreeCodeGenerator.evaluateParseTree(
    //                                 guardParseTree, _scope);

    //                         codeBuffer.append(parseTreeCodeGenerator
    //                                 .generateFireCode());

    //                         codeBuffer.append(") ");
    //                     }
    //                 }
    //                 codeBuffer.append(_eol + "{" + _eol);

    //                 // generate code for choice action
    //                 for (AbstractActionsAttribute action : (List<AbstractActionsAttribute>) transition
    //                         .choiceActionList()) {

    //                     Iterator channelNumberList = action.getChannelNumberList()
    //                             .iterator();
    //                     Iterator parseTreeList = action.getParseTreeList()
    //                             .iterator();

    //                     for (String destinationName : (List<String>) action
    //                             .getDestinationNameList()) {

    //                         Integer channelNumber = (Integer) channelNumberList
    //                                 .next();
    //                         ASTPtRootNode parseTree = (ASTPtRootNode) parseTreeList
    //                                 .next();
    //                         NamedObj destination = action
    //                                 .getDestination(destinationName);

    //                         int channel = -1;
    //                         if (channelNumber != null) {
    //                             channel = channelNumber.intValue();
    //                         }

    //                         StringBuffer sendCode = new StringBuffer();

    //                         // Note in choice action only output can be generated
    //                         // and no parameter be changed.
    //                         if (channel >= 0) {
    //                             //codeBuffer.append("$ref(" + destinationName + "#"
    //                             //        + channel + ") = ");
    //                             codeBuffer.append("$put(" + destinationName + "#"
    //                                     + channel + ", ");

    //                             // During choice action, an output port
    //                             // receives token sent by itself when it
    //                             // is also an input port, i.e., when this
    //                             // FSMActor is used as a modal controller.

    //                             if (((IOPort) destination).isInput()) {
    //                                 //ComponentCodeGenerator containerHelper = _getHelper(((IOPort) destination)
    //                                 //      .getContainer().getContainer());

    //                                 NamedProgramCodeGeneratorAdapter containerHelper = (NamedProgramCodeGeneratorAdapter) getCodeGenerator()
    //                                         .getAdapter(
    //                                                 ((IOPort) destination)
    //                                                         .getContainer()
    //                                                         .getContainer());

    //                                 StringBuffer containerReference = new StringBuffer();

    //                                 //containerReference.append("$ref("
    //                                 //        + generateSimpleName(destination));
    //                                 containerReference.append("$get("
    //                                         + generateSimpleName(destination));

    //                                 if (((IOPort) destination).isMultiport()) {
    //                                     containerReference.append("#" + channel);
    //                                 }

    //                                 containerReference.append(",");

    //                                 codeBuffer.append(containerHelper
    //                                         .processCode(containerReference
    //                                                 .toString())
    //                                         + ")");

    //                                 sendCode.append("$send(" + destinationName
    //                                         + ", " + channel + ")" + _eol);
    //                             }
    //                         } else { // broadcast

    //                             int width = ((IOPort) action
    //                                     .getDestination(destinationName))
    //                                     .getWidth();

    //                             for (int i = 0; i < width; i++) {
    //                                 codeBuffer.append("$put(" + destinationName
    //                                         + "#" + i + ", ");

    //                                 // During choice action, an output
    //                                 // port receives token sent by itself
    //                                 // when it is also an input port,
    //                                 // i.e., when this FSMActor is used as
    //                                 // a modal controller.

    //                                 if (((IOPort) destination).isInput()) {
    //                                     //ComponentCodeGenerator containerHelper = _getHelper(((IOPort) destination)
    //                                     //      .getContainer().getContainer());

    //                                     //                                    NamedProgramCodeGeneratorAdapter containerHelper = (NamedProgramCodeGeneratorAdapter) getCodeGenerator()
    //                                     //                                            .getAdapter(
    //                                     //                                                    ((IOPort) destination)
    //                                     //                                                            .getContainer()
    //                                     //                                                            .getContainer());

    //                                     //                                    StringBuffer containerReference = new StringBuffer();
    //                                     //
    //                                     //                                    //containerReference.append("$ref("
    //                                     //                                    //        + generateSimpleName(destination));
    //                                     //                                    containerReference.append("$new("
    //                                     //                                            + generateSimpleName(destination));
    //                                     //
    //                                     //                                    if (((IOPort) destination).isMultiport()) {
    //                                     //                                        containerReference.append("#" + i);
    //                                     //                                    }
    //                                     //
    //                                     //                                    containerReference.append(")");
    //                                     //
    //                                     //                                    codeBuffer.append(containerHelper
    //                                     //                                            .processCode(containerReference
    //                                     //                                                    .toString())
    //                                     //                                            + " = ");

    //                                     //sendCode.append("$send("
    //                                     //        + generateSimpleName(destination)
    //                                     //        + ", " + i + ")" + _eol);
    //                                 }
    //                             }
    //                         }
    //                         ParseTreeCodeGenerator parseTreeCodeGenerator = getTemplateParser()
    //                                 .getParseTreeCodeGenerator();
    //                         parseTreeCodeGenerator.evaluateParseTree(parseTree,
    //                                 _scope);
    //                         codeBuffer.append(parseTreeCodeGenerator
    //                                 .generateFireCode());

    //                         codeBuffer.append(");" + _eol);
    //                         //codeBuffer.append(sendCode);
    //                     }
    //                 }

    //                 // generate code for transition refinement
    //                 Actor[] actors = transition.getRefinement();

    //                 if (actors != null) {
    //                     for (Actor actor : actors) {
    //                         NamedProgramCodeGeneratorAdapter helper = (NamedProgramCodeGeneratorAdapter) getAdapter(actor);
    //                         // fire the actor
    //                         codeBuffer.append(helper.generateFireCode());
    //                     }
    //                 }

    //                 // generate code for commit action
    //                 for (AbstractActionsAttribute action : (List<AbstractActionsAttribute>) transition
    //                         .commitActionList()) {

    //                     Iterator channelNumberList = action.getChannelNumberList()
    //                             .iterator();
    //                     Iterator parseTreeList = action.getParseTreeList()
    //                             .iterator();

    //                     for (String destinationName : (List<String>) action
    //                             .getDestinationNameList()) {

    //                         Integer channelNumber = (Integer) channelNumberList
    //                                 .next();
    //                         ASTPtRootNode parseTree = (ASTPtRootNode) parseTreeList
    //                                 .next();
    //                         NamedObj destination = action
    //                                 .getDestination(destinationName);

    //                         int channel = -1;
    //                         if (channelNumber != null) {
    //                             channel = channelNumber.intValue();
    //                         }

    //                         ParseTreeCodeGenerator parseTreeCodeGenerator = getTemplateParser()
    //                                 .getParseTreeCodeGenerator();
    //                         parseTreeCodeGenerator.evaluateParseTree(parseTree,
    //                                 _scope);
    //                         String scopeFireCode = parseTreeCodeGenerator
    //                                 .generateFireCode();

    //                         if (destination instanceof IOPort) {
    //                             if (channel >= 0) {
    //                                 //codeBuffer.append("$ref(" + destinationName
    //                                 //        + "#" + channel + ") = ");
    //                                 codeBuffer.append("$put(" + destinationName
    //                                         + "#" + channel + ", "
    //                                         + scopeFireCode + ");");
    //                             } else { // broadcast

    //                                 int width = ((IOPort) action
    //                                         .getDestination(destinationName))
    //                                         .getWidth();

    //                                 for (int i = 0; i < width; i++) {
    //                                     //codeBuffer.append("$ref(" + destinationName
    //                                     //        + "#" + i + ") = ");
    //                                     codeBuffer.append("$put(" + destinationName
    //                                             + "#" + i + ", " + scopeFireCode
    //                                             + ");");
    //                                 }
    //                             }
    //                         } else if (destination instanceof Variable) {
    //                             codeBuffer.append(getCodeGenerator()
    //                                     .generateVariableName(destination)
    //                                     + " = "
    //                                     + scopeFireCode + ";");
    //                         }
    //                     }
    //                 }

    //                 // generate code for updating current state
    //                 State destinationState = transition.destinationState();
    //                 _updateCurrentState(codeBuffer, destinationState);

    //                 // generate code for reinitialization if history is
    //                 // false.  we assume the value of history itself cannot
    //                 // be changed dynamically

    //                 if (!transition.isHistory()) {
    //                     actors = destinationState.getRefinement();

    //                     if (actors != null) {
    //                         for (int i = 0; i < actors.length; ++i) {
    //                             NamedProgramCodeGeneratorAdapter helper = (NamedProgramCodeGeneratorAdapter) getAdapter(actors[i]);

    //                             codeBuffer.append(helper.generateInitializeCode());
    //                         }
    //                     }
    //                 }

    //                 codeBuffer.append(_eol + "}" + _eol);
    //             }

    //             if (!hasDefaultCase) {
    //                 if (transitionCount > 0) {
    //                     codeBuffer.append("else" + _eol + "{" + _eol);
    //                 } else {
    //                     codeBuffer.append(_eol);
    //                 }

    //                 // indicates no transition is taken.
    //                 codeBuffer.append(_eol + modalName + "__transitionFlag = 0;"
    //                         + _eol);

    //                 // Generate code for updating configuration number of this
    //                 // FSMActor's container.  Note we need this because the
    //                 // configuration of the current refinement may have been
    //                 // changed even when there is no state transition.  The
    //                 // code is generated only when this FSMActor is used as a
    //                 // modal controller for an instance of
    //                 // MultirateFSMDirector.

    //                 //Director director = fsmActor.getExecutiveDirector();
    //                 //if (director instanceof ptolemy.domains.modal.kernel.MultirateFSMDirector) {
    //                 //     MultirateFSMDirector directorHelper = (MultirateFSMDirector) _getHelper(director);
    //                 //   directorHelper
    //                 //         ._updateConfigurationNumber(codeBuffer, state);
    //                 //}

    //                 if (transitionCount > 0) {
    //                     codeBuffer.append(_eol + "}" + _eol); // end of if statement
    //                 }
    //             }
    //             codeBuffer.append(_eol + "break;" + _eol); // end of case statement
    //         }

    //         codeBuffer.append(_eol + "}" + _eol); // end of switch statement
    //         code.append(TemplateParser.unescapeName(processCode(codeBuffer
    //                 .toString()))); // was initially enclosed with processCode()
    //     }

    //     /** A class implementing this interface implements a method to
    //      *  retrieve transitions of a given state. Depending on
    //      *  implementation, it could return all transitions, only
    //      *  preemptive transitions or only non-preemptive transitions.
    //      */
    //     public static interface TransitionRetriever {

    //         /** Returns an iterator of (some or all) transitions from the
    //          * given state.
    //          *  @param state The given state.
    //          *  @return An iterator of (some or all) transitions from the
    //          *  given state.
    //          */
    //         public Iterator retrieveTransitions(State state);
    //     }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    //     /** Generate the fire code of the associated FSMActor.  It provides
    //      *  generateTransitionCode(StringBuffer, TransitionRetriever) with an
    //      *  anonymous class implementing a method which returns an iterator of
    //      *  all outgoing transitions of the current state.
    //      *
    //      *  @return The generated fire code.
    //      *  @exception IllegalActionException If thrown while generating
    //      *  firing code.
    //      */
    //     protected String _generateFireCode() throws IllegalActionException {

    //         StringBuffer code = new StringBuffer();
    //         //code.append(super._generateFireCode());
    //         code.append(getCodeGenerator().comment("FSMActor._generateFireCode()"));

    //         ptolemy.domains.modal.kernel.FSMActor fsmActor = (ptolemy.domains.modal.kernel.FSMActor) getComponent();

    //         //         // FIXME: not handling multirate inputs yet.
    //         //         // FIXME: how should we handle in-out ports?
    //         System.out.println("FSMActor()._generateFireCode(): about to get inputs");
    //         code.append(getCodeGenerator().comment("generateFireCode(): generating ports"));
    //         for (IOPort input : (List<IOPort>) fsmActor.inputPortList()) {
    //             for (int channel = 0; !input.isOutput()
    //                      && channel < input.getWidth(); channel++) {
    //                 code.append(generateName(input) + " = $get("
    //                         + generateSimpleName(input) + ", "
    //                         + channel + ");" + _eol);
    //             }
    //         }
    //         code.append(getCodeGenerator().comment("generateFireCode(): done generating ports"));
    //         this.generateTransitionCode(code, new OutgoingRelations());

    //         return processCode(code.toString());
    //     }

    /** Generate a label for a state constant.
     *  @param state The state.
     *  @return The label.
     */
    @Override
    protected Object _generateStateConstantLabel(State state) {
        return generateName(state);
    }

}
