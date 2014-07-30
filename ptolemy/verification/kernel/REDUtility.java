/* An utility function for traversing the system and generate files for model checking using Regional Decision Diagram (RED).

 Copyright (c) 1998-2014 The Regents of the University of California.
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
package ptolemy.verification.kernel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedActor;
import ptolemy.actor.lib.Clock;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.domains.de.kernel.DEDirector;
import ptolemy.domains.de.lib.TimedDelay;
import ptolemy.domains.modal.kernel.FSMActor;
import ptolemy.domains.modal.kernel.State;
import ptolemy.domains.modal.kernel.Transition;
import ptolemy.domains.modal.modal.ModalModel;
import ptolemy.kernel.ComponentPort;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.verification.kernel.MathematicalModelConverter.FormulaType;
import ptolemy.verification.lib.BoundedBufferNondeterministicDelay;
import ptolemy.verification.lib.BoundedBufferTimedDelay;

///////////////////////////////////////////////////////////////////
// //REDUtility

/**
 * This is an utility for Ptolemy model conversion. It performs a systematic
 * traversal of the system and convert the Ptolemy model into communicating
 * timed automata (CTA) with the format acceptable by model checker RED
 * (Regional Encoding Diagram Verification Engine, v.7.0).
 * <p>
 * The conversion mechanism roughly is based on the technical report UCB/EECS-2008-41
 * with some modifications. Basically, the token would not
 * be accumulated in the port of the FSMActor - therefore buffer overflow
 * property would no longer exist in this implementation; it
 * would only happen in the TimedDelay or NondeterministicTimedDelay actor.
 * <p>
 * For a successful conversion, we simply disallow a system to
 * have super dense time tag with the format (\tau, i), where i>0.
 * In our context this only happens when there is a TimedDelay actor with its
 * parameter equals to zero. For systems with super dense time tag with the
 * format (\tau, i), where i>0, the system can still be converted. However,
 * please note that the semantics might no longer be preserved.
 * <p>
 * One important feature in our converted model is the use of "complementary"
 * edges. This is used to handle the situation where the FSMActor must react
 * to an arrival of token in the incoming port, but the token can not trigger
 * any transition. For this case, the 'present' token should turn to be 'absent'
 * as time advances. To avoid including any unnecessary behavior we add one
 * "invalid" transition, mentioning that the FSMActor will perform a "stable"
 * move, and at the same time the token will be bring to absent state.
 * <p>
 * For the tool RED, all time constants should be an integer. This is not a
 * problem because the unit is actually not specified by the timed automata.
 * Therefore, we expect users to use integer values to specify their delay
 * or period.
 * <p>
 * Limitations: Simply following the statement in the technical report, we
 * restate limitations of the conversion. The designer must understand the
 * boundary of variable domain for the model under conversion. Also, due to
 * the use of complementary edges, complex guard conditions
 * are currently not supported.
 *
 * @deprecated ptolemy.de.lib.TimedDelay is deprecated, use ptolemy.actor.lib.TimeDelay.
 * @author Chih-Hong Cheng, Contributor: Edward A. Lee
 * @version $Id$
 * @since Ptolemy II 8.0
 * @Pt.ProposedRating Red (patrickj)
 * @Pt.AcceptedRating Red (patrickj)
 */
@Deprecated
public class REDUtility {

    /**
     * This function generates an equivalent system which is flattened.
     * It would perform a rewriting of each ModalModel with hierarchy to an
     * FSMActor. Note that in our current implementation the rewriting
     * only supports 'state refinements'.
     *
     * @param originalCompositeActor original system under processing
     * @return a flattened equivalent system.
     */
    public static CompositeActor generateEquivalentSystemWithoutHierarchy(
            CompositeActor originalCompositeActor)
                    throws NameDuplicationException, IllegalActionException,
                    CloneNotSupportedException {

        ArrayList<FSMActor> list = new ArrayList<FSMActor>();

        if (originalCompositeActor.entityList().size() > 0) {
            Iterator it = originalCompositeActor.entityList().iterator();
            while (it.hasNext()) {
                Entity innerEntity = (Entity) it.next();
                if (innerEntity instanceof ModalModel) {
                    // If the innerEntity is a ModalModel, try to rewrite it.
                    FSMActor newActor = _rewriteModalModelWithStateRefinementToFSMActor((ModalModel) innerEntity);
                    // Remove the original ModalModel from the system
                    // (we would add an equivalent FSMActor back later).
                    originalCompositeActor.entityList().remove(innerEntity);
                    // Add the newly generated FSMActor to the list.
                    list.add(newActor);
                }
            }
        }

        for (int i = 0; i < list.size(); i++) {
            // Add back those previously generated new FSMActors.
            originalCompositeActor.entityList().add(list.get(i));
        }
        return originalCompositeActor;

    }

    /**
     * This is the main function which generates the system description
     * which is acceptable by the tool RED (Regional Encoding Diagram).
     * <p>
     * For hierarchical conversion, here we are able to deal with cases where
     * state refinement exists. For a modalmodel with state refinement, we first
     * rewrite it into an equivalent FSMActor.
     *
     * @param PreModel The original model in Ptolemy II
     * @param pattern The temporal formula in TCTL
     * @param choice Specify the type of formula: buffer overflow detection or
     *               general TCTL formula
     * @param span The size of the span used for domain analysis.
     * @param bufferSizeDelayActor The buffer size of the TimedDelay actor.
     * @return A Communicating Timed Automata system description of the original
     *         system
     * @exception IllegalActionException
     */
    public static StringBuffer generateREDDescription(CompositeActor PreModel,
            String pattern, FormulaType choice, int span,
            int bufferSizeDelayActor) throws IllegalActionException,
            NameDuplicationException, CloneNotSupportedException {

        StringBuffer returnREDFormat = new StringBuffer("");

        // A pre-processing to generate equivalent system without hierarchy.
        CompositeActor model = generateEquivalentSystemWithoutHierarchy(PreModel);

        // The format of RED 7.0 is roughly organized as follows:
        //
        // (1) Constant Value Definition (#define CLOCK 1)
        // (2) Process Count Definition (Process count = 8;)
        // (3) Variable Declaration (global discrete a:0..8)
        // (4) Clock Declaration (global clock c)
        // (5) Synchronizer Declaration (global synchronizer s;)
        // (6) Mode description
        // (7) Initial Condition (initially...)
        // (8) Risk Condition (safety/risk/...): in 7.0 this has been removed out as
        //     a separate file.
        //
        // Because in our conversion process, we analyze actors one by
        // one during iteration, thus we need to use different variables
        // to store different contents in each phase, then combine
        // all these information to form a complete format.
        //

        StringBuffer constantDefinition = new StringBuffer(""); // (1)
        StringBuffer variableDefinition = new StringBuffer(""); // (3)
        ArrayList<String> globalClockSet = new ArrayList<String>(); // (4)
        StringBuffer moduleDefinition = new StringBuffer(""); // (6)
        HashSet<String> globalSynchronizerSet = new HashSet<String>(); // (5)
        HashSet<String> variableAndItsInitialCondition = new HashSet<String>(); // (7)

        // We need to record the ordering of Modules and Ports; these will
        // be processed later because the ordering decides the initial state
        // of each process.
        ArrayList<REDModuleNameInitialBean> processModuleNameList = new ArrayList<REDModuleNameInitialBean>();
        ArrayList<REDModuleNameInitialBean> processModulePortList = new ArrayList<REDModuleNameInitialBean>();

        // Perform a search to determine the all useful synchronizers used
        // in the system.
        for (Iterator actors = model.entityList().iterator(); actors.hasNext();) {
            Entity innerEntity = (Entity) actors.next();

            HashSet<String> setOfSynchronizes = _decideSynchronizerVariableSet(innerEntity);
            Iterator<String> it = setOfSynchronizes.iterator();
            while (it.hasNext()) {
                globalSynchronizerSet.add(it.next());
            }
        }

        // For different kind of actors, the utility function would try to
        // call different processing functions.
        // Currently we are able to deal with the following actors:
        // (1) FSMActor
        // (2) BoundedBufferNondeterministicDelay
        // (3) BoundedBufferTimedDelay
        // (4) Clock
        //
        // Note that the order is important. This is because
        // BoundedBufferNondeterministicDelay extends BoundedBufferTimedDelay.
        // If we place these two in the wrong order, it would always treat
        // BoundedBufferNondeterministicDelay as BoundedBufferTimedDelay.
        //

        for (Iterator actors = model.entityList().iterator(); actors.hasNext();) {
            Entity innerEntity = (Entity) actors.next();
            if (innerEntity instanceof FSMActor) {

                // Set up the return bean for data storage
                REDSingleEntityBean bean = _translateFSMActor(
                        (FSMActor) innerEntity, span, globalSynchronizerSet);
                variableDefinition.append(bean._declaredVariables);
                constantDefinition.append(bean._defineConstants);
                Iterator<String> it = bean._clockSet.iterator();
                while (it.hasNext()) {
                    globalClockSet.add(it.next());
                }
                Iterator<REDModuleNameInitialBean> portlists = bean._portSet
                        .iterator();
                while (portlists.hasNext()) {
                    processModulePortList.add(portlists.next());
                }
                Iterator<String> variableInitialValues = bean._variableInitialDescriptionSet
                        .iterator();
                while (variableInitialValues.hasNext()) {
                    variableAndItsInitialCondition.add(variableInitialValues
                            .next());
                }

                moduleDefinition.append(bean._moduleDescription);
                processModuleNameList.add(bean._nameInitialState);

            } else if (innerEntity instanceof BoundedBufferNondeterministicDelay) {

                // Because for BoundedBufferNondeterministicDelay, the port name
                // is not modifiable. Thus we need to offer a mechanism to
                // understand the port name for the conjuncted signal.
                // For example, in Sec---->BBNondeterministicDelay, we would use
                // Sec as the incoming signal name.

                // Decide output signal name
                String outputSignalName = null;
                Iterator outputConnectedPortList = ((BoundedBufferNondeterministicDelay) innerEntity).output
                        .connectedPortList().iterator();

                while (outputConnectedPortList.hasNext()) {
                    String portName = ((Port) outputConnectedPortList.next())
                            .getName();
                    if (portName
                            .equalsIgnoreCase(((BoundedBufferNondeterministicDelay) innerEntity).output
                                    .getName().trim())) {
                        continue;
                    } else {
                        outputSignalName = portName;
                    }
                }
                // Decide input signal name
                String inputSignalName = null;
                Iterator inputConnectedPortList = ((BoundedBufferNondeterministicDelay) innerEntity).input
                        .connectedPortList().iterator();

                while (inputConnectedPortList.hasNext()) {
                    String portName = ((Port) inputConnectedPortList.next())
                            .getName();
                    if (portName
                            .equalsIgnoreCase(((BoundedBufferNondeterministicDelay) innerEntity).input
                                    .getName().trim())) {
                        continue;
                    } else {
                        inputSignalName = portName;
                    }
                }

                REDSingleEntityBean bean = _translateBBNondeterministicDelayedActor(
                        (BoundedBufferNondeterministicDelay) innerEntity,
                        inputSignalName, outputSignalName);
                variableDefinition.append(bean._declaredVariables);
                constantDefinition.append(bean._defineConstants);
                processModuleNameList.add(bean._nameInitialState);

                Iterator<String> it = bean._clockSet.iterator();
                while (it.hasNext()) {
                    globalClockSet.add(it.next());
                }

                Iterator<REDModuleNameInitialBean> ports = bean._portSet
                        .iterator();
                while (ports.hasNext()) {
                    processModulePortList.add(ports.next());
                }
                moduleDefinition.append(bean._moduleDescription);

                Iterator<String> variableInitialValues = bean._variableInitialDescriptionSet
                        .iterator();
                while (variableInitialValues.hasNext()) {
                    variableAndItsInitialCondition.add(variableInitialValues
                            .next());
                }

            } else if (innerEntity instanceof BoundedBufferTimedDelay) {

                // Decide the name of input and output signal
                String outputSignalName = null;
                Iterator outputConnectedPortList = ((BoundedBufferTimedDelay) innerEntity).output
                        .connectedPortList().iterator();

                while (outputConnectedPortList.hasNext()) {
                    String portName = ((Port) outputConnectedPortList.next())
                            .getName();
                    if (portName
                            .equalsIgnoreCase(((BoundedBufferTimedDelay) innerEntity).output
                                    .getName().trim())) {
                        continue;
                    } else {
                        outputSignalName = portName;
                    }
                }
                String inputSignalName = null;
                Iterator inputConnectedPortList = ((BoundedBufferTimedDelay) innerEntity).input
                        .connectedPortList().iterator();

                while (inputConnectedPortList.hasNext()) {
                    String portName = ((Port) inputConnectedPortList.next())
                            .getName();
                    if (portName
                            .equalsIgnoreCase(((BoundedBufferTimedDelay) innerEntity).input
                                    .getName().trim())) {
                        continue;
                    } else {
                        inputSignalName = portName;
                    }
                }

                REDSingleEntityBean bean = _translateBBTimedDelayedActor(
                        (BoundedBufferTimedDelay) innerEntity, inputSignalName,
                        outputSignalName);
                constantDefinition.append(bean._defineConstants);
                variableDefinition.append(bean._declaredVariables);
                processModuleNameList.add(bean._nameInitialState);
                Iterator<String> clocks = bean._clockSet.iterator();
                while (clocks.hasNext()) {
                    globalClockSet.add(clocks.next());
                }

                moduleDefinition.append(bean._moduleDescription);

                Iterator<String> variableInitialValues = bean._variableInitialDescriptionSet
                        .iterator();
                while (variableInitialValues.hasNext()) {
                    variableAndItsInitialCondition.add(variableInitialValues
                            .next());
                }
            } else if (innerEntity instanceof TimedDelay) {

                // Decide the name of input and output signal
                String outputSignalName = null;
                Iterator outputConnectedPortList = ((TimedDelay) innerEntity).output
                        .connectedPortList().iterator();

                while (outputConnectedPortList.hasNext()) {
                    String portName = ((Port) outputConnectedPortList.next())
                            .getName();
                    if (portName
                            .equalsIgnoreCase(((TimedDelay) innerEntity).output
                                    .getName().trim())) {
                        continue;
                    } else {
                        outputSignalName = portName;
                    }
                }
                String inputSignalName = null;
                Iterator inputConnectedPortList = ((TimedDelay) innerEntity).input
                        .connectedPortList().iterator();

                while (inputConnectedPortList.hasNext()) {
                    String portName = ((Port) inputConnectedPortList.next())
                            .getName();
                    if (portName
                            .equalsIgnoreCase(((TimedDelay) innerEntity).input
                                    .getName().trim())) {
                        continue;
                    } else {
                        inputSignalName = portName;
                    }
                }

                REDSingleEntityBean bean = _translateTimedDelayedActor(
                        (TimedDelay) innerEntity, inputSignalName,
                        outputSignalName, bufferSizeDelayActor);
                constantDefinition.append(bean._defineConstants);
                variableDefinition.append(bean._declaredVariables);
                processModuleNameList.add(bean._nameInitialState);
                Iterator<String> clocks = bean._clockSet.iterator();
                while (clocks.hasNext()) {
                    globalClockSet.add(clocks.next());
                }

                moduleDefinition.append(bean._moduleDescription);

                Iterator<String> variableInitialValues = bean._variableInitialDescriptionSet
                        .iterator();
                while (variableInitialValues.hasNext()) {
                    variableAndItsInitialCondition.add(variableInitialValues
                            .next());
                }
            } else if (innerEntity instanceof Clock) {
                String outputSignalName = null;
                Iterator outputConnectedPortList = ((Clock) innerEntity).output
                        .connectedPortList().iterator();

                while (outputConnectedPortList.hasNext()) {
                    String portName = ((Port) outputConnectedPortList.next())
                            .getName();
                    if (portName.equalsIgnoreCase(((Clock) innerEntity).output
                            .getName().trim())) {
                        continue;
                    } else {
                        outputSignalName = portName;
                    }
                }

                REDSingleEntityBean bean = _translateClockActor(
                        (Clock) innerEntity, outputSignalName);

                variableDefinition.append(bean._declaredVariables);
                constantDefinition.append(bean._defineConstants);

                moduleDefinition.append(bean._moduleDescription);
                processModuleNameList.add(bean._nameInitialState);

                Iterator<String> clocks = bean._clockSet.iterator();
                while (clocks.hasNext()) {
                    globalClockSet.add(clocks.next());
                }

                Iterator<String> variableInitialValues = bean._variableInitialDescriptionSet
                        .iterator();
                while (variableInitialValues.hasNext()) {
                    variableAndItsInitialCondition.add(variableInitialValues
                            .next());
                }
            }
        }

        // Lastly, combine the whole format based on the order of the RED format.
        // First, attach a comment indicating the description.
        returnREDFormat.append("/*\n\n"
                + "This file represents a Communicating Timed Automata (CTA)\n"
                + "representation for a model described by Ptolemy II.\n"
                + "It is compatible with the format of the tool \"Regional\n"
                + "Encoding Diagram\" (RED 7.0) which is an integrated \n"
                + "symbolic TCTL model-checker/simulator.\n\n");
        // Retrieve the value in the processModuleNameList to understand
        // the corresponding name in the process. Also in RED, the first process
        // starts at the number 1.
        for (int i = 0; i < processModuleNameList.size(); i++) {
            returnREDFormat.append("Process " + String.valueOf(i + 1) + ": "
                    + processModuleNameList.get(i)._name + "\n");
        }
        for (int i = 0; i < processModulePortList.size(); i++) {
            returnREDFormat.append("Process "
                    + String.valueOf(processModuleNameList.size() + i + 1)
                    + ": " + processModulePortList.get(i)._name + "\n");
        }
        returnREDFormat.append("\n*/\n\n"); // end of comment

        // Constant definition
        returnREDFormat.append(constantDefinition + "\n\n");

        // List out the number of processes.
        returnREDFormat.append("process count = "
                + String.valueOf(processModuleNameList.size()
                        + processModulePortList.size()) + ";\n\n");

        // Variable definition
        returnREDFormat.append(variableDefinition + "\n\n");
        // Clock definition
        if (globalClockSet.size() != 0) {
            returnREDFormat.append("global clock  ");
            Iterator<String> it = globalClockSet.iterator();
            while (it.hasNext()) {
                String sync = it.next();
                if (it.hasNext()) {
                    returnREDFormat.append(sync + ", ");
                } else {
                    returnREDFormat.append(sync + ";\n");
                }
            }
        }
        returnREDFormat.append("local clock t;\n ");

        // Synchronizer definition
        if (globalSynchronizerSet.size() != 0) {
            returnREDFormat.append("\nglobal synchronizer ");
            Iterator<String> it = globalSynchronizerSet.iterator();
            while (it.hasNext()) {
                String sync = it.next();
                returnREDFormat.append(sync + ", ");
            }
        }

        for (Iterator actors = model.entityList().iterator(); actors.hasNext();) {
            Entity innerEntity = (Entity) actors.next();
            if (innerEntity instanceof FSMActor) {
                returnREDFormat.append("N_" + innerEntity.getName().trim()
                        + ", ");
            }
        }

        returnREDFormat.append("tick ;\n ");

        // Module description
        returnREDFormat
        .append(moduleDefinition
                + "\n/*State representing buffer overflow. */\nmode Buffer_Overflow (true) {\n}\n");

        // Initial Condition: Except FSMActors, the rest should be the same.
        returnREDFormat.append("\n/*Initial Condition */\ninitially\n");
        for (int i = 0; i < processModuleNameList.size(); i++) {
            returnREDFormat.append("    "
                    + processModuleNameList.get(i)._initialStateDescription
                    + "[" + String.valueOf(i + 1) + "]" + " && " + "t["
                    + String.valueOf(i + 1) + "] == 0 && \n");
        }
        for (int i = 0; i < processModulePortList.size(); i++) {
            returnREDFormat.append("    "
                    + processModulePortList.get(i)._initialStateDescription
                    + "["
                    + String.valueOf(processModuleNameList.size() + i + 1)
                    + "]" + " && " + "t["
                    + String.valueOf(processModuleNameList.size() + i + 1)
                    + "] == 0 && \n");
        }
        // Set up all variables with their initial value.
        Iterator<String> ite = variableAndItsInitialCondition.iterator();
        while (ite.hasNext()) {
            String sync = ite.next();
            returnREDFormat.append("    " + sync + " && \n ");
        }
        // Set up all clocks as zero
        Iterator<String> it = globalClockSet.iterator();
        while (it.hasNext()) {
            String clock = it.next();
            if (it.hasNext()) {
                returnREDFormat.append("    " + clock + " == 0 && \n ");
            } else {
                returnREDFormat.append("    " + clock + " == 0 ;\n ");
            }
        }

        // Specification: Deprecated in RED 7.0
        returnREDFormat.append("\n/*Specification */\n");
        if (choice == FormulaType.Buffer) {
            returnREDFormat
            .append("/* In RED 7.0, specification must be placed in separated files. */\n/* risk\nexists i:i>=1, (Buffer_Overflow[i]);*/\n\n");

        } else {
            returnREDFormat
            .append("/* In RED 7.0, specification must be placed in separated files. */\n/*"
                    + pattern + "*/\n");

        }
        return returnREDFormat;
    }

    /**
     * This function decides if the director of the current actor is DE. If not,
     * return false. This is because our current conversion to CTA is only valid
     * when the director is DE.
     *
     * @param model Model used for testing.
     * @return boolean value indicating if the director is DE.
     */
    public static boolean isValidModelForVerification(CompositeActor model) {
        Director director = model.getDirector();
        if (!(director instanceof DEDirector)) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * This private function is used by the private function _translateFSMActor
     * to generate the set of signals used in the guard expression. Each of the
     * signal used by the guard expression would need to have a process
     * representing the port receiving the signal.
     *
     * @param actor The actor under analysis.
     * @return Set of signals used in guard expressions in the FSMActor.
     * @exception IllegalActionException
     */
    private static HashSet<String> _decideGuardSignalVariableSet(FSMActor actor)
            throws IllegalActionException {

        HashSet<String> returnVariableSet = new HashSet<String>();
        HashSet<State> stateSet = new HashSet<State>();
        // initialize
        HashMap<String, State> frontier = new HashMap<String, State>();

        // create initial state
        State stateInThis = actor.getInitialState();
        String name = stateInThis.getName();
        frontier.put(name, stateInThis);

        // iterate
        while (!frontier.isEmpty()) {
            Iterator<String> iterator = frontier.keySet().iterator();
            name = iterator.next();
            stateInThis = frontier.remove(name);
            if (stateInThis == null) {
                throw new IllegalActionException("Internal error, removing \""
                        + name + "\" returned null?");
            }
            ComponentPort outPort = stateInThis.outgoingPort;
            Iterator transitions = outPort.linkedRelationList().iterator();

            while (transitions.hasNext()) {
                Transition transition = (Transition) transitions.next();

                State destinationInThis = transition.destinationState();

                if (!stateSet.contains(destinationInThis)) {
                    frontier.put(destinationInThis.getName(), destinationInThis);
                    stateSet.add(destinationInThis);
                }

                // get transitionLabel, transitionName and relation
                // name for later use. Retrieve the transition

                boolean hasAnnotation = false;
                String text;
                try {
                    text = transition.annotation.stringValue();
                } catch (IllegalActionException e) {
                    text = "Exception evaluating annotation: " + e.getMessage();
                }
                if (!text.trim().equals("")) {
                    hasAnnotation = true;
                    // buffer.append(text);
                }

                // Retrieve the guardExpression for checking the existence
                // of inner variables used in FmcAutomaton.
                String guard = transition.getGuardExpression();

                if (guard != null && !guard.trim().equals("")) {
                    if (guard.trim().equalsIgnoreCase("true")) {
                        // If the guard is true, then it can be triggered by any arrival of the signal
                        for (int i = 0; i < actor.inputPortList().size(); i++) {
                            returnVariableSet.add(((IOPort) actor
                                    .inputPortList().get(i)).getName());
                        }
                        return returnVariableSet;
                    }
                    if (hasAnnotation) {
                        // do nothing
                    } else {
                        // Separate each guard expression into substring
                        String[] guardSplitExpression = guard.split("(&&)");
                        if (guardSplitExpression.length != 0) {
                            for (String element : guardSplitExpression) {

                                String subGuardCondition = element.trim();
                                // Retrieve the left value of the inequality.
                                String[] characterOfSubGuard = subGuardCondition
                                        .split("(>=)|(<=)|(==)|(!=)|[><]");
                                // Here we may still have two cases:
                                // (1) XXX_isPresent (2) the normal case.
                                boolean b = Pattern.matches(".*_isPresent",
                                        characterOfSubGuard[0].trim());
                                if (b == true) {
                                    String[] sigs = characterOfSubGuard[0]
                                            .trim().split("_isPresent");
                                    if (returnVariableSet.contains(sigs[0]) == false) {
                                        returnVariableSet.add(sigs[0]);
                                    }
                                }

                            }

                        }
                    }
                }
            }
        }
        return returnVariableSet;
    }

    /**
     * This private function is used to decide the set of global synchronizers
     * used in the entity. When we later return the set, the system would use
     * another set container to store the synchronizer to make sure that no
     * duplication exists.
     *
     * @param entity entity under analysis
     * @return The set of synchronizers used in this entity
     * @exception IllegalActionException If thrown while getting the initial state
     * of the actor.
     */
    private static HashSet<String> _decideSynchronizerVariableSet(Entity entity)
            throws IllegalActionException {
        HashSet<String> returnVariableSet = new HashSet<String>();

        if (entity instanceof FSMActor) {
            HashSet<State> stateSet = new HashSet<State>();

            // initialize
            HashMap<String, State> frontier = new HashMap<String, State>();

            // create initial state
            State stateInThis = ((FSMActor) entity).getInitialState();
            String name = stateInThis.getName();
            frontier.put(name, stateInThis);

            // iterate
            while (!frontier.isEmpty()) {
                // Pick a state from frontier. It seems that there isn't an
                // easy way to pick an arbitrary entry from a HashMap, except
                // through Iterator
                Iterator<String> iterator = frontier.keySet().iterator();
                name = iterator.next();
                stateInThis = frontier.remove(name);
                if (stateInThis == null) {
                    throw new IllegalActionException(
                            "Internal error, removing \"" + name
                            + "\" returned null?");
                }
                ComponentPort outPort = stateInThis.outgoingPort;
                Iterator transitions = outPort.linkedRelationList().iterator();

                while (transitions.hasNext()) {
                    Transition transition = (Transition) transitions.next();

                    State destinationInThis = transition.destinationState();

                    if (!stateSet.contains(destinationInThis)) {
                        frontier.put(destinationInThis.getName(),
                                destinationInThis);
                        stateSet.add(destinationInThis);
                    }

                    // get transitionLabel, transitionName and relation
                    // name for later use. Retrieve the transition

                    boolean hasAnnotation = false;
                    String text;
                    try {
                        text = transition.annotation.stringValue();
                    } catch (IllegalActionException e) {
                        text = "Exception evaluating annotation: "
                                + e.getMessage();
                    }
                    if (!text.trim().equals("")) {
                        hasAnnotation = true;
                        // buffer.append(text);
                    }
                    // Retrieve the guardExpression for checking the existence
                    // of inner variables used in FmcAutomaton.
                    String guard = transition.getGuardExpression();
                    if (guard != null && !guard.trim().equals("")) {
                        if (hasAnnotation) {
                            // do nothing
                        } else {
                            // Separate each guard expression into substring
                            String[] guardSplitExpression = guard.split("(&&)");
                            if (guardSplitExpression.length != 0) {
                                for (String element : guardSplitExpression) {
                                    String subGuardCondition = element.trim();
                                    // Retrieve the left value of the
                                    // inequality.
                                    String[] characterOfSubGuard = subGuardCondition
                                            .split("(>=)|(<=)|(==)|(!=)|[><]");
                                    // Here we may still have two cases:
                                    // (1) XXX_isPresent (2) the normal case.
                                    boolean b = Pattern.matches(".*_isPresent",
                                            characterOfSubGuard[0].trim());
                                    if (b == true) {
                                        String[] sigs = characterOfSubGuard[0]
                                                .trim().split("_isPresent");
                                        // When in a FSM, it has an edge showing XX_isPresent, we add up
                                        // two synchronizers XX and ND_XX. XX is the signal from
                                        // outside to the port, and ND_XX represents the forwarded signal without delay.
                                        if (returnVariableSet.contains(sigs[0]) == false) {
                                            returnVariableSet.add(sigs[0]);
                                        }
                                        if (returnVariableSet.contains("ND_"
                                                + sigs[0]) == false) {

                                            returnVariableSet.add("ND_"
                                                    + sigs[0]);
                                        }
                                        if (returnVariableSet.contains("Token"
                                                + sigs[0] + "Consume") == false) {
                                            returnVariableSet.add("Token"
                                                    + sigs[0] + "Consume");
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

        } else if (entity instanceof TimedDelay) {
            String outputSignalName = null;
            Iterator outputConnectedPortList = ((TimedDelay) entity).output
                    .connectedPortList().iterator();

            while (outputConnectedPortList.hasNext()) {
                String portName = ((Port) outputConnectedPortList.next())
                        .getName();
                if (portName.equalsIgnoreCase(((TimedDelay) entity).output
                        .getName().trim())) {
                    continue;
                } else {
                    outputSignalName = portName;
                }
            }
            String inputSignalName = null;
            Iterator inputConnectedPortList = ((TimedDelay) entity).input
                    .connectedPortList().iterator();

            while (inputConnectedPortList.hasNext()) {
                String portName = ((Port) inputConnectedPortList.next())
                        .getName();
                if (portName.equalsIgnoreCase(((TimedDelay) entity).input
                        .getName().trim())) {
                    continue;
                } else {
                    inputSignalName = portName;
                }
            }
            returnVariableSet.add(inputSignalName);
            returnVariableSet.add(outputSignalName);
        } else if (entity instanceof Clock) {
            String outputSignalName = null;
            Iterator outputConnectedPortList = ((Clock) entity).output
                    .connectedPortList().iterator();

            while (outputConnectedPortList.hasNext()) {
                String portName = ((Port) outputConnectedPortList.next())
                        .getName();
                if (portName.equalsIgnoreCase(((Clock) entity).output.getName()
                        .trim())) {
                    continue;
                } else {
                    outputSignalName = portName;
                }
            }
            returnVariableSet.add(outputSignalName);
        }
        return returnVariableSet;
    }

    /**
     * This private function decides inner variables used in the actor. It later
     * perform a systematic scan to generate the initial rough domain, and use a
     * constant span to expand it.
     *
     * @param actor Actor under analysis.
     * @param numSpan The size of the span to expand the variable domain.
     * @return The set of variables (variable names) used in the FSMActor.
     * @exception IllegalActionException
     */
    private static HashSet<String> _decideVariableSet(FSMActor actor,
            int numSpan) throws IllegalActionException {

        HashSet<String> returnVariableSet = new HashSet<String>();
        HashSet<State> stateSet = new HashSet<State>();

        // initialize
        HashMap<String, State> frontier = new HashMap<String, State>();
        _variableInfo = new HashMap<String, VariableInfo>();

        // create initial state
        State stateInThis = actor.getInitialState();
        String name = stateInThis.getName();
        frontier.put(name, stateInThis);

        // iterate
        while (!frontier.isEmpty()) {
            // pick a state from frontier. It seems that there isn't an
            // easy way to pick an arbitrary entry from a HashMap, except
            // through Iterator
            Iterator<String> iterator = frontier.keySet().iterator();
            name = iterator.next();
            stateInThis = frontier.remove(name);
            if (stateInThis == null) {
                throw new IllegalActionException("Internal error, removing \""
                        + name + "\" returned null?");
            }
            ComponentPort outPort = stateInThis.outgoingPort;
            Iterator transitions = outPort.linkedRelationList().iterator();

            while (transitions.hasNext()) {
                Transition transition = (Transition) transitions.next();

                State destinationInThis = transition.destinationState();

                if (!stateSet.contains(destinationInThis)) {
                    frontier.put(destinationInThis.getName(), destinationInThis);
                    stateSet.add(destinationInThis);
                }

                // get transitionLabel, transitionName and relation
                // name for later use. Retrieve the transition

                boolean hasAnnotation = false;
                String text;
                try {
                    text = transition.annotation.stringValue();
                } catch (IllegalActionException e) {
                    text = "Exception evaluating annotation: " + e.getMessage();
                }
                if (!text.trim().equals("")) {
                    hasAnnotation = true;

                }

                // Retrieve the guardExpression for checking the existence
                // of inner variables used in FmcAutomaton.
                String guard = transition.getGuardExpression();
                if (guard != null && !guard.trim().equals("")) {
                    if (hasAnnotation) {
                        // do nothing
                    } else {

                        // Separate each guard expression into substring
                        String[] guardSplitExpression = guard.split("(&&)");
                        if (guardSplitExpression.length != 0) {
                            for (String element : guardSplitExpression) {

                                String subGuardCondition = element.trim();
                                // Retrieve the left value of the
                                // inequality.
                                String[] characterOfSubGuard = subGuardCondition
                                        .split("(>=)|(<=)|(==)|(!=)|[><]");
                                // Here we may still have two cases:
                                // (1) XXX_isPresent (2) the normal case.
                                boolean b = Pattern.matches(".*_isPresent",
                                        characterOfSubGuard[0].trim());
                                if (b == true) {
                                    // First case, synchronize usage.
                                    // Currently do nothing for single
                                    // FmvAutomaton case.
                                } else {
                                    // Second case, place this variable into
                                    // usage set. Retrieve the rvalue

                                    // Check if the right value exists. We
                                    // need to ward off cases like "true".

                                    String rValue = null;
                                    boolean isTrue = false;
                                    try {
                                        rValue = characterOfSubGuard[1].trim();
                                    } catch (Exception ex) {
                                        isTrue = true;
                                    }
                                    if (isTrue == false) {
                                        if (Pattern.matches("^-?\\d+$", rValue) == true) {
                                            int numberRetrieval = Integer
                                                    .parseInt(rValue);
                                            // add it into the _variableInfo
                                            returnVariableSet
                                            .add(characterOfSubGuard[0]
                                                    .trim());
                                            VariableInfo variable = _variableInfo
                                                    .get(characterOfSubGuard[0]
                                                            .trim());
                                            if (variable == null) {
                                                // Create a new one and
                                                // insert all info.
                                                VariableInfo newVariable = new VariableInfo(

                                                        Integer.toString(numberRetrieval),
                                                        Integer.toString(numberRetrieval));
                                                _variableInfo.put(
                                                        characterOfSubGuard[0]
                                                                .trim(),
                                                                newVariable);

                                            } else {
                                                // modify the existing one
                                                if (Integer
                                                        .parseInt(variable._maxValue) < numberRetrieval) {
                                                    variable._maxValue = Integer
                                                            .toString(numberRetrieval);
                                                }
                                                if (Integer
                                                        .parseInt(variable._minValue) > numberRetrieval) {
                                                    variable._minValue = Integer
                                                            .toString(numberRetrieval);
                                                }
                                                _variableInfo
                                                .remove(characterOfSubGuard[0]
                                                        .trim());
                                                _variableInfo.put(
                                                        characterOfSubGuard[0]
                                                                .trim(),
                                                                variable);

                                            }
                                        }
                                    }
                                }

                            }

                        }
                    }

                }

                String expression = transition.setActions.getExpression();
                if (expression != null && !expression.trim().equals("")) {
                    // Retrieve possible value of the variable
                    String[] splitExpression = expression.split(";");
                    for (String element : splitExpression) {
                        String[] characters = element.split("=");
                        if (characters.length >= 1) {
                            String lValue = characters[0].trim();
                            if (Pattern.matches("^-?\\d+$",
                                    characters[1].trim()) == true) {
                                int numberRetrieval = Integer
                                        .parseInt(characters[1].trim());
                                // add it into the _variableInfo

                                VariableInfo variable = _variableInfo
                                        .get(lValue);
                                if (variable == null) {
                                    // Create a new one and insert all info.
                                    VariableInfo newVariable = new VariableInfo(
                                            Integer.toString(numberRetrieval),
                                            Integer.toString(numberRetrieval));
                                    _variableInfo.put(lValue, newVariable);

                                } else {
                                    // modify the existing one
                                    if (Integer.parseInt(variable._maxValue) < numberRetrieval) {
                                        variable._maxValue = Integer
                                                .toString(numberRetrieval);
                                    }
                                    if (Integer.parseInt(variable._minValue) > numberRetrieval) {
                                        variable._minValue = Integer
                                                .toString(numberRetrieval);
                                    }
                                    _variableInfo.remove(lValue);
                                    _variableInfo.put(lValue, variable);

                                }
                            }
                        }

                    }
                }

            }

        }

        Iterator<String> initialValueIterator = _variableInfo.keySet()
                .iterator();
        while (initialValueIterator.hasNext()) {
            String variableName = initialValueIterator.next();
            if (Pattern.matches(".*_isPresent", variableName)) {
                continue;
            }
            String property = null;
            boolean initialValueExist = true;
            String[] propertyList = null;
            try {
                propertyList = actor.getAttribute(variableName).description()
                        .split(" ");
            } catch (Exception ex) {
                initialValueExist = false;
            }
            if (initialValueExist == true) {
                // Retrieve the value of the variable. Property contains
                // a huge trunk of string content, and only the last variable is
                // useful.
                property = propertyList[propertyList.length - 1];
            } else {
                property = "";
            }

            VariableInfo variableInfo = _variableInfo.get(variableName);

            if (Pattern.matches("^-?\\d+$", property) == true) {
                if (variableInfo != null) {
                    if (variableInfo._minValue != null
                            && variableInfo._maxValue != null) {
                        int lowerBound = Integer
                                .parseInt(variableInfo._minValue);
                        int upperBound = Integer
                                .parseInt(variableInfo._maxValue);
                        // modify the existing one
                        if (upperBound < Integer.parseInt(property)) {
                            variableInfo._maxValue = property;
                        }
                        if (lowerBound > Integer.parseInt(property)) {
                            variableInfo._minValue = property;
                        }
                    }
                }
            }
        }

        // Expend based on the domain
        Iterator<String> itVariableSet = returnVariableSet.iterator();
        while (itVariableSet.hasNext()) {

            String valName = itVariableSet.next();

            // Retrieve the lower bound and upper bound of the variable used in
            // the system based on inequalities or assignments
            VariableInfo individual = _variableInfo.remove(valName);
            try {
                if (individual != null) {
                    if (individual._maxValue != null
                            && individual._minValue != null) {
                        int lbOriginal = Integer.parseInt(individual._minValue);
                        int ubOriginal = Integer.parseInt(individual._maxValue);
                        int lbNew = lbOriginal - (ubOriginal - lbOriginal + 1)
                                * numSpan;
                        int ubNew = ubOriginal + (ubOriginal - lbOriginal + 1)
                                * numSpan;
                        individual._minValue = Integer.toString(lbNew);
                        individual._maxValue = Integer.toString(ubNew);
                        _variableInfo.put(valName, individual);
                    }
                }

            } catch (Exception ex) {
                throw new IllegalActionException(
                        "REDUtility._decideVariableSet() clashes: "
                                + ex.getMessage());
            }
        }

        return returnVariableSet;
    }

    /**
     * Perform an enumeration of the state in this actor and return the name of
     * the states. It seems to have a better way to do this (a mechanism to
     * enumerate using existing member functions).
     *
     * @param actor The actor under analysis
     * @return The set of states of the FSMActor.
     * @exception IllegalActionException
     */
    private static HashSet<State> _enumerateStateSet(FSMActor actor)
            throws IllegalActionException {

        HashSet<State> returnStateSet = new HashSet<State>();
        try {
            // init
            HashMap<String, State> frontier = new HashMap<String, State>();

            // create initial state
            State stateInThis = actor.getInitialState();
            String name = stateInThis.getName();
            frontier.put(name, stateInThis);
            returnStateSet.add(stateInThis);
            // iterate
            while (!frontier.isEmpty()) {
                // pick a state from frontier. It seems that there isn't an
                // easy way to pick an arbitrary entry from a HashMap, except
                // through Iterator
                Iterator<String> iterator = frontier.keySet().iterator();
                name = iterator.next();
                stateInThis = frontier.remove(name);
                ComponentPort outPort = stateInThis.outgoingPort;
                Iterator transitions = outPort.linkedRelationList().iterator();

                while (transitions.hasNext()) {
                    Transition transition = (Transition) transitions.next();

                    State destinationInThis = transition.destinationState();
                    if (!returnStateSet.contains(destinationInThis)) {
                        frontier.put(destinationInThis.getName(),
                                destinationInThis);
                        returnStateSet.add(destinationInThis);
                    }
                }
            }
        } catch (Exception exception) {
            throw new IllegalActionException(
                    "REDUtility._EnumerateStateSet() clashes: "
                            + exception.getMessage());

        }
        return returnStateSet;
    }

    /**
     * This private function generates all possible combinations of
     * string with length i with character 0 and 1. For example, for i = 2 it
     * would generate {00, 01, 10, 11}. This is designed to invoke recursively
     * to achieve this goal.
     *
     * @param index The size of the index.
     * @param paraEnumerateString
     *                Existing strings that need to be attached.
     * @return An list of all possible combination for char 0 and 1 of size
     *         index.
     */
    private static ArrayList<String> _enumerateString(int index,
            ArrayList<String> paraEnumerateString) {
        ArrayList<String> returnEnumerateString = new ArrayList<String>();
        if (index == 0) {
            return paraEnumerateString;
        } else {
            for (String temp : paraEnumerateString) {
                returnEnumerateString.add("0" + temp);
                returnEnumerateString.add("1" + temp);
            }
        }
        return _enumerateString(index - 1, returnEnumerateString);
    }

    /**
     * This private function is used to generate the transition description for
     * a certain state in a certain actor. The output format is CTA acceptable
     * by model checker RED.
     *
     * @param actor Actor under analysis
     * @param state State under analysis
     * @param variableSet
     * @param globalSynchronizerSet
     *                Set of useful synchronizers. There are some synchronizers
     *                which is not useful. They are not connected to/from a
     *                valid actor where analysis is possible.
     * @return A set of transition descriptions packed in a list.
     * @exception IllegalActionException
     */
    private static ArrayList<REDTransitionBean> _generateTransition(
            FSMActor actor, State state, HashSet<String> variableSet,
            HashSet<String> globalSynchronizerSet)
                    throws IllegalActionException {

        ArrayList<REDTransitionBean> returnList = new ArrayList<REDTransitionBean>();

        List entityList = actor.entityList();
        Iterator it = entityList.iterator();
        while (it.hasNext()) {
            Entity entity = (Entity) it.next();
            if (entity instanceof State) {
                if (entity.getName().equalsIgnoreCase(state.getName())) {
                    ComponentPort outPort = ((State) entity).outgoingPort;
                    Iterator transitions = outPort.linkedRelationList()
                            .iterator();
                    while (transitions.hasNext()) {

                        Transition transition = (Transition) transitions.next();
                        State destinationInThis = transition.destinationState();
                        REDTransitionBean bean = new REDTransitionBean();

                        HashSet<String> usedSignalInTransition = new HashSet<String>();

                        bean._newState.append(actor.getName() + "_State_"
                                + destinationInThis.getName());

                        boolean hasAnnotation = false;
                        String text;
                        try {
                            text = transition.annotation.stringValue();
                        } catch (IllegalActionException e) {
                            text = "Exception evaluating annotation: "
                                    + e.getMessage();
                        }
                        if (!text.trim().equals("")) {
                            hasAnnotation = true;
                            // buffer.append(text);
                        }

                        // Retrieve the variable used in the CTA.
                        // Also analyze the guard expression to understand the
                        // possible value domain for the value to execute.
                        //
                        // A guard expression would need to be separated into
                        // separate sub-statements in order to estimate the
                        // boundary of the variable. Note that we need to
                        // tackle cases where a>-1 and a<5 happen simultaneously.
                        // Also we expect to constrain the way that an end user can do for writing
                        // codes. We do "not" expect him to write in the way
                        // like -1<a.
                        //
                        // Also here we assume that every sub-guard expression
                        // is connected using && but not || operator. But it is
                        // easy to modify the code such that it supports ||.
                        //

                        String guard = transition.getGuardExpression();

                        String outputAction = transition.outputActions
                                .getExpression();

                        // variableUsedInTransitionSet: Store variable names
                        // used in this transition as preconditions. If in the guard
                        // expression, we have X<3 && Y>5, then X and Y are used
                        // as variables in precondition and should be stored in the
                        // set "variableUsedInTransitionSet".

                        if (guard != null
                                && guard.trim().equalsIgnoreCase("true")) {
                            // Special case for true; in this way, the system must listen to all incoming ports
                            // Since each incoming port is OK, it will turn to be separate transitions.
                            bean._preCondition.append("true");
                            // For this condition, no complementary edges are needed.
                        } else if (guard != null && !guard.trim().equals("")) {
                            if (hasAnnotation) {

                            } else {
                                String[] guardSplitExpression = guard
                                        .split("(&&)");

                                if (guardSplitExpression.length != 0) {
                                    for (String element : guardSplitExpression) {
                                        // Trim tab/space
                                        String subGuardCondition = element
                                                .trim();

                                        // Retrieve the left value of the
                                        // inequality. Here we may still have
                                        // two cases for the lValue:
                                        // (1) XXX_isPresent (2) the normal case
                                        // (including "true").
                                        String[] characterOfSubGuard = subGuardCondition
                                                .split("(>=)|(<=)|(==)|(!=)|[><]");

                                        String lValue = characterOfSubGuard[0]
                                                .trim();
                                        boolean b = Pattern.matches(
                                                ".*_isPresent",
                                                characterOfSubGuard[0].trim());
                                        if (b == true) {
                                            // First case, synchronize usage.
                                            // Pgo_isPresent
                                            // We add it into the list for transition.

                                            String[] signalName = characterOfSubGuard[0]
                                                    .trim().split("_isPresent");
                                            if (bean._signal.toString()
                                                    .equalsIgnoreCase("")) {
                                                bean._signal.append(" ?ND_"
                                                        + signalName[0]);
                                            } else {
                                                bean._signal.append("  ?ND_"
                                                        + signalName[0]);
                                            }
                                            bean._signalSet.add("?ND_"
                                                    + signalName[0].trim());
                                            // specify the fact that this transition has signal.
                                            // if a transition does not have a signal, it can be triggered by "any" signal

                                            usedSignalInTransition
                                            .add(signalName[0]);
                                        } else {
                                            // Split the expression, and rename
                                            // the variable by adding up the
                                            // name of the module.

                                            // Check if the right value exists.
                                            // We need to ward off cases like "true".
                                            // This is achieved using try-catch
                                            // and retrieve the rValue from
                                            // characterOfSubGuard[1].
                                            boolean parse = true;
                                            String rValue = null;
                                            try {
                                                rValue = characterOfSubGuard[1]
                                                        .trim();
                                            } catch (Exception ex) {
                                                parse = false;
                                            }
                                            if (parse == true) {
                                                if (Pattern.matches("^-?\\d+$",
                                                        rValue) == true) {

                                                    // We need to understand what is
                                                    // the operator of the value
                                                    // in order to reason the bound
                                                    // of the variable for suitable
                                                    // transition.

                                                    if (Pattern.matches(
                                                            ".*==.*",
                                                            subGuardCondition)) {
                                                        // equal than, restrict
                                                        // the set of all possible
                                                        // values in the domain
                                                        // into one single value.
                                                        if (bean._preCondition
                                                                .toString()
                                                                .equalsIgnoreCase(
                                                                        "")) {
                                                            bean._preCondition
                                                            .append(actor
                                                                    .getName()
                                                                    + "_"
                                                                    + lValue
                                                                    + " == "
                                                                    + rValue);
                                                        } else {
                                                            bean._preCondition
                                                            .append(" && "
                                                                    + actor.getName()
                                                                    + "_"
                                                                    + lValue
                                                                    + " == "
                                                                    + rValue);
                                                        }
                                                        bean._complementedCondition
                                                        .add(actor
                                                                .getName()
                                                                + "_"
                                                                + lValue
                                                                + " != "
                                                                + rValue);
                                                    } else if (Pattern.matches(
                                                            ".*!=.*",
                                                            subGuardCondition)) {
                                                        if (bean._preCondition
                                                                .toString()
                                                                .equalsIgnoreCase(
                                                                        "")) {
                                                            bean._preCondition
                                                            .append(actor
                                                                    .getName()
                                                                    + "_"
                                                                    + lValue
                                                                    + " != "
                                                                    + rValue);
                                                        } else {
                                                            bean._preCondition
                                                            .append(" && "
                                                                    + actor.getName()
                                                                    + "_"
                                                                    + lValue
                                                                    + " != "
                                                                    + rValue);
                                                        }
                                                        bean._complementedCondition
                                                        .add(actor
                                                                .getName()
                                                                + "_"
                                                                + lValue
                                                                + " == "
                                                                + rValue);

                                                    } else if (Pattern.matches(
                                                            ".*<=.*",
                                                            subGuardCondition)) {
                                                        if (bean._preCondition
                                                                .toString()
                                                                .equalsIgnoreCase(
                                                                        "")) {
                                                            bean._preCondition
                                                            .append(actor
                                                                    .getName()
                                                                    + "_"
                                                                    + lValue
                                                                    + " <= "
                                                                    + rValue);
                                                        } else {
                                                            bean._preCondition
                                                            .append(" && "
                                                                    + actor.getName()
                                                                    + "_"
                                                                    + lValue
                                                                    + " <= "
                                                                    + rValue);
                                                        }
                                                        bean._complementedCondition
                                                        .add(actor
                                                                .getName()
                                                                + "_"
                                                                + lValue
                                                                + " > "
                                                                + rValue);
                                                    } else if (Pattern.matches(
                                                            ".*>=.*",
                                                            subGuardCondition)) {
                                                        // greater or equal than
                                                        if (bean._preCondition
                                                                .toString()
                                                                .equalsIgnoreCase(
                                                                        "")) {
                                                            bean._preCondition
                                                            .append(actor
                                                                    .getName()
                                                                    + "_"
                                                                    + lValue
                                                                    + " >= "
                                                                    + rValue);
                                                        } else {
                                                            bean._preCondition
                                                            .append(" && "
                                                                    + actor.getName()
                                                                    + "_"
                                                                    + lValue
                                                                    + " >= "
                                                                    + rValue);
                                                        }
                                                        bean._complementedCondition
                                                        .add(actor
                                                                .getName()
                                                                + "_"
                                                                + lValue
                                                                + " < "
                                                                + rValue);
                                                    } else if (Pattern.matches(
                                                            ".*>.*",
                                                            subGuardCondition)) {
                                                        // greater than
                                                        if (bean._preCondition
                                                                .toString()
                                                                .equalsIgnoreCase(
                                                                        "")) {
                                                            bean._preCondition
                                                            .append(actor
                                                                    .getName()
                                                                    + "_"
                                                                    + lValue
                                                                    + " > "
                                                                    + rValue);
                                                        } else {
                                                            bean._preCondition
                                                            .append(" && "
                                                                    + actor.getName()
                                                                    + "_"
                                                                    + lValue
                                                                    + " > "
                                                                    + rValue);
                                                        }
                                                        bean._complementedCondition
                                                        .add(actor
                                                                .getName()
                                                                + "_"
                                                                + lValue
                                                                + " <= "
                                                                + rValue);

                                                    } else if (Pattern.matches(
                                                            ".*<.*",
                                                            subGuardCondition)) {
                                                        // less than
                                                        if (bean._preCondition
                                                                .toString()
                                                                .equalsIgnoreCase(
                                                                        "")) {
                                                            bean._preCondition
                                                            .append(actor
                                                                    .getName()
                                                                    + "_"
                                                                    + lValue
                                                                    + " < "
                                                                    + rValue);
                                                        } else {
                                                            bean._preCondition
                                                            .append(" && "
                                                                    + actor.getName()
                                                                    + "_"
                                                                    + lValue
                                                                    + " < "
                                                                    + rValue);

                                                        }
                                                        bean._complementedCondition
                                                        .add(actor
                                                                .getName()
                                                                + "_"
                                                                + lValue
                                                                + " >= "
                                                                + rValue);
                                                    }

                                                } else {
                                                    throw new IllegalActionException(
                                                            "REDUtility._generateTransition() clashes: Currently verification only support simple guards.");
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        String setActionExpression = transition.setActions
                                .getExpression();

                        if (setActionExpression != null
                                && !setActionExpression.trim().equals("")) {
                            // Retrieve possible value of the variable
                            String[] splitExpression = setActionExpression
                                    .split(";");
                            for (String element : splitExpression) {
                                String[] characters = element.split("=");
                                if (characters.length >= 1) {

                                    String lValue = characters[0].trim();
                                    String rValue = "";
                                    if (Pattern.matches("^-?\\d+$",
                                            characters[1].trim()) == true) {
                                        rValue = characters[1].trim();
                                        bean._postCondition.append(actor
                                                .getName()
                                                + "_"
                                                + lValue
                                                + " = " + rValue + ";");
                                    } else {
                                        // The right hand side is actually complicated
                                        // expression which needs to be carefully
                                        // designed for accepting various expression.
                                        // If we expect to do this, it is necessary
                                        // to construct a parse tree and evaluate the value.
                                        // Currently let us assume that we are
                                        // manipulating simple format a = a op constInt;
                                        // or a = constInt;
                                        rValue = characters[1].trim();
                                        if (Pattern.matches(".*\\*.*", rValue)) {
                                            String[] rValueOperends = rValue
                                                    .split("\\*");
                                            bean._postCondition.append(actor
                                                    .getName()
                                                    + "_"
                                                    + lValue
                                                    + " = "
                                                    + actor.getName()
                                                    + "_"
                                                    + rValueOperends[0].trim()
                                                    + " * "
                                                    + rValueOperends[1].trim()
                                                    + ";");
                                        } else if (Pattern.matches(".*/.*",
                                                rValue)) {
                                            String[] rValueOperends = rValue
                                                    .split("[/]");
                                            bean._postCondition.append(actor
                                                    .getName()
                                                    + "_"
                                                    + lValue
                                                    + " = "
                                                    + actor.getName()
                                                    + "_"
                                                    + rValueOperends[0].trim()
                                                    + " / "
                                                    + rValueOperends[1].trim()
                                                    + ";");
                                        } else if (Pattern.matches(".*\\+.*",
                                                rValue)) {
                                            String[] rValueOperends = rValue
                                                    .split("\\+");
                                            bean._postCondition.append(actor
                                                    .getName()
                                                    + "_"
                                                    + lValue
                                                    + " = "
                                                    + actor.getName()
                                                    + "_"
                                                    + rValueOperends[0].trim()
                                                    + " + "
                                                    + rValueOperends[1].trim()
                                                    + ";");
                                        } else if (Pattern.matches(".*\\-.*",
                                                rValue)) {
                                            String[] rValueOperends = rValue
                                                    .split("\\-");
                                            bean._postCondition.append(actor
                                                    .getName()
                                                    + "_"
                                                    + lValue
                                                    + " = "
                                                    + actor.getName()
                                                    + "_"
                                                    + rValueOperends[0].trim()
                                                    + " - "
                                                    + rValueOperends[1].trim()
                                                    + ";");
                                        }
                                    }
                                }
                            }
                        }

                        // Generate the complementary edge which consumes token and perform the still move.
                        Iterator<IOPort> it2 = actor.inputPortList().iterator();
                        while (it2.hasNext()) {
                            String signal = it2.next().getName();
                            if (usedSignalInTransition.contains(signal) == false) {
                                // Generate the complementary edge with stationary move
                                REDTransitionBean newBean = new REDTransitionBean();
                                newBean._isComplementaryEdge = true;
                                newBean._newState.append(actor.getName()
                                        + "_State_"
                                        + transition.sourceState().getName());
                                // Port_" + signalName.trim() + "_TokenOccupied == true
                                newBean._signal.append(" !Token"
                                        + signal.trim() + "Consume");
                                newBean._signalSet.add("?ND_"
                                        + signal.trim().trim());
                                if (guard != null
                                        && guard.trim()
                                        .equalsIgnoreCase("true")) {
                                    // Special case for true; in this way, the system must listen to all incoming ports
                                    // Since each incoming port is OK, it will turn to be separate transitions.
                                    newBean._preCondition.append("false");
                                } else if (guard != null
                                        && !guard.trim().equals("")) {
                                }

                                // Note that no postconditions are needed.
                                returnList.add(newBean);
                            }
                        }

                        if (outputAction != null
                                && !outputAction.trim().equals("")) {
                            String[] outputActionSplitExpression = outputAction
                                    .split("(;)");
                            if (outputActionSplitExpression.length != 0) {
                                for (String element : outputActionSplitExpression) {
                                    String[] characterOfSubOutput = element
                                            .split("=");
                                    String lValue = characterOfSubOutput[0]
                                            .trim();
                                    if (globalSynchronizerSet.contains(lValue) == true) {
                                        if (bean._signal.toString()
                                                .equalsIgnoreCase("")) {
                                            bean._signal.append("!" + lValue);
                                        } else {
                                            bean._signal.append("  !" + lValue);
                                        }
                                    }
                                }
                            }
                        }
                        returnList.add(bean);
                        // Add the bean to the returnList
                    }
                }
            }
        }

        // Last step: Finish the guard condition for each complemented edge.
        // Currently these edges are only with synchronizers. Conditions for
        // constraining is as follows. Pick the complemented edge, check the set of
        // synchronizers. Find all original edges using the same synchronizer,
        // and conjunct with the negated condition.

        for (int i = 0; i < returnList.size(); i++) {
            if (returnList.get(i)._isComplementaryEdge == true) {
                // Search for all edges, find all non-complementary edges, if their signal
                // is the same as the signal in the current edge, add the complementary
                // guard conditions.

                for (int j = 0; j < returnList.size(); j++) {
                    if (returnList.get(j)._signalSet
                            .equals(returnList.get(i)._signalSet)) {
                        /* Add all negations of the guard in j to the guard of i*/
                        for (int k = 0; k < returnList.get(j)._complementedCondition
                                .size(); k++) {
                            if (returnList.get(i)._preCondition.toString()
                                    .equalsIgnoreCase("")) {
                                returnList.get(i)._preCondition
                                .append(returnList.get(j)._complementedCondition
                                        .get(k));
                            } else {
                                returnList.get(i)._preCondition
                                .append(" && "
                                        + returnList.get(j)._complementedCondition
                                        .get(k));
                            }
                        }
                    }
                }
            }
        }

        return returnList;

    }

    /**
     * A private function for generating initial values for the
     * initial variable set. The current approach is to retrieve them
     * from the parameter specified in the actor.
     *
     * @param actor
     * @param variableSet
     * @return The variable initial value set.
     */
    private static HashMap<String, String> _retrieveVariableInitialValue(
            FSMActor actor, HashSet<String> variableSet) {
        // One problem regarding the initial value retrieval from parameters
        // is that when retrieving parameters, the return value would consist
        // of some undesirable information. We need to use split to do further
        // analysis.

        // FIXME: One potential problem happens when a user forgets
        // to specify the parameter. We need to establish a
        // mechanism to report this case (it is not difficult).

        HashMap<String, String> returnMap = new HashMap<String, String>();
        Iterator<String> it = variableSet.iterator();
        while (it.hasNext()) {
            String attribute = it.next();
            String property = null;
            boolean initialValueExist = true;
            String[] propertyList = null;
            try {
                propertyList = actor.getAttribute(attribute).description()
                        .split(" ");
            } catch (Exception ex) {
                initialValueExist = false;
            }
            if (initialValueExist == true) {
                property = propertyList[propertyList.length - 1];
            } else {
                property = "";
            }

            // Retrieve the value of the variable. Property contains
            // a huge trunk of string content, and only the last variable is
            // useful.

            returnMap.put(attribute, property);
        }
        return returnMap;

    }

    /**
     * This is an experimental function which analyzes a ModalModel and
     * flatten it into a single FSMActor. As model checkers
     * in timed systems are using models without hierarchy, this function is required.
     * <p>
     * In our current implementation we only allow one additional layer for the
     * refinement. Also the additional layer must be a finite state
     * refinement so that the conversion is possible. But it is easy to expand
     * this functionality into multiple layer.
     * <p>
     * Note that in the current context of ModalModel, when state machine
     * refinement is used, it is not possible to generate further refinements,
     * meaning that the current implementation is powerful enough to deal with
     * state refinements.
     *
     * @param modalmodel Whole System under analysis.
     * @return Equivalent FSMActor for later analysis.
     */
    private static FSMActor _rewriteModalModelWithStateRefinementToFSMActor(
            ModalModel modalmodel) throws IllegalActionException,
            NameDuplicationException, CloneNotSupportedException {

        // The algorithm is roughly constructed as follows:
        //
        // Step 1: For each state, check if there is refinement.
        // If there is refinement, then jump into the refinement.
        // Copy the content of the refinement into the new FSMActor.
        //
        // Step 2: Scan all transition from the original actor; if there is a
        // transition from state A to state B with condition C, then
        // for every refinement state in A, there is a transition
        // to the initial refinement state of B with transition C.
        //
        // Note that we try to generate a similar FSMActor instead of modifying
        // existing one; thus we offer utility functions which performs deep
        // copy of states, transitions, and FSMActors.

        FSMActor model = modalmodel.getController();
        FSMActor returnFSMActor = new FSMActor(model.workspace());
        // try {
        returnFSMActor.setName(modalmodel.getName());
        Iterator states = model.entityList().iterator();
        while (states.hasNext()) {
            NamedObj state = (NamedObj) states.next();
            if (state instanceof State) {
                String refinementList = ((State) state).refinementName
                        .getExpression();
                if (refinementList == null) {
                    // Copy the state into the returnFSMActor.
                    // This is done in a reverse way, that is,
                    // set the container of the state instead of
                    // adding component of a FSMActor.
                    State newState = (State) state.clone();
                    newState.setName(state.getName());
                    newState.setContainer(returnFSMActor);
                    if (model.getInitialState() == state) {
                        newState.isInitialState.setToken("true");
                    }
                    newState.moveToFirst();

                } else if (refinementList.equalsIgnoreCase("")) {
                    // Copy the state into the returnFSMActor.
                    // This is done in a reverse way, that is,
                    // set the container of the state instead of
                    // adding component of a FSMActor.
                    // Interesting :)
                    State newState = (State) state.clone();
                    newState.setName(state.getName());
                    newState.setContainer(returnFSMActor);
                    if (model.getInitialState() == state) {
                        newState.isInitialState.setToken("true");
                    }
                    newState.moveToFirst();
                } else {
                    // First check of the refinement is state refinement.
                    // This can be checked by getting the refinement.

                    TypedActor[] actors = ((State) state).getRefinement();
                    if (actors != null) {
                        if (actors.length > 1) {

                            System.out
                            .println("We might not be able to deal with it");
                        } else {

                            // Retrieve the actor.
                            TypedActor innerActor = actors[0];
                            if (innerActor instanceof FSMActor) {
                                // Retrieve all states and place into returnFSMActor
                                Iterator innerStates = ((FSMActor) innerActor)
                                        .entityList().iterator();
                                while (innerStates.hasNext()) {
                                    NamedObj innerState = (NamedObj) innerStates
                                            .next();
                                    if (innerState instanceof State) {
                                        // We need to give it a new name based
                                        // on our criteria.
                                        // For example state S has refinement,
                                        // then a state S' in the refinement should
                                        // be renamed as S-S' for the analysis usage.

                                        State newState = (State) innerState
                                                .clone();

                                        newState.setName(state.getName().trim()
                                                + "-"
                                                + innerState.getName().trim());

                                        newState.setContainer(returnFSMActor);
                                        if (model.getInitialState() == state
                                                && ((FSMActor) innerActor)
                                                .getInitialState() == innerState) {
                                            newState.isInitialState
                                            .setToken("true");
                                        }
                                        newState.moveToFirst();

                                    }
                                }

                                // We also need to glue transitions into the
                                // system.

                                Iterator innerTransitions = ((FSMActor) innerActor)
                                        .relationList().iterator();
                                while (innerTransitions.hasNext()) {
                                    Relation innerTransition = (Relation) innerTransitions
                                            .next();

                                    if (!(innerTransition instanceof Transition)) {
                                        continue;
                                    }

                                    State source = ((Transition) innerTransition)
                                            .sourceState();
                                    State destination = ((Transition) innerTransition)
                                            .destinationState();

                                    Transition newTransition = (Transition) innerTransition
                                            .clone();

                                    newTransition.setName(((State) state)
                                            .getName()
                                            + "-"
                                            + innerTransition.getName());
                                    // We need to attach states to it.
                                    // The newly attached states should be in
                                    // returnFSMActor.
                                    Iterator returnFSMActorStates = returnFSMActor
                                            .entityList().iterator();
                                    State sCorresponding = null;
                                    State dCorresponding = null;
                                    while (returnFSMActorStates.hasNext()) {
                                        NamedObj cState = (NamedObj) returnFSMActorStates
                                                .next();
                                        if (cState instanceof State) {

                                            if (((State) cState)
                                                    .getName()
                                                    .equalsIgnoreCase(
                                                            ((State) state)
                                                            .getName()
                                                            .trim()
                                                            + "-"
                                                            + source.getName()
                                                            .trim())) {

                                                sCorresponding = (State) cState;
                                            }
                                        }
                                    }
                                    returnFSMActorStates = returnFSMActor
                                            .entityList().iterator();
                                    while (returnFSMActorStates.hasNext()) {
                                        NamedObj cState = (NamedObj) returnFSMActorStates
                                                .next();
                                        if (cState instanceof State) {

                                            if (((State) cState)
                                                    .getName()
                                                    .equalsIgnoreCase(
                                                            ((State) state)
                                                            .getName()
                                                            .trim()
                                                            + "-"
                                                            + destination
                                                            .getName()
                                                            .trim())) {
                                                dCorresponding = (State) cState;

                                            }
                                        }
                                    }

                                    Port s = sCorresponding.outgoingPort;
                                    Port d = dCorresponding.incomingPort;
                                    newTransition.unlinkAll();
                                    newTransition.setContainer(returnFSMActor);
                                    newTransition.moveToFirst();
                                    s.link(newTransition);
                                    d.link(newTransition);
                                }

                            } else {
                                // Currently this is beyond our scope for
                                // analysis.
                                throw new IllegalActionException(
                                        "It is currently allowed to have general refinement of states.");
                            }
                        }
                    } else {
                        // This should not happen
                        throw new IllegalActionException(
                                "REDUtility._rewriteModalModelWithStateRefinementToFSMActor() clashes: actors is null");

                    }
                }
            }
        }

        // Now we have returnFSMActor having a flatten set of states.
        // However, the transition is not complete. This is because we
        // haven't establish the connection of between inner actors.
        //
        // For each state S in the inner actor, if its upper state A has
        // a transition from A to another state B, then S must establish
        // a transition which connects itself with B; if B has refinements,
        // then S must establish a connection which connects to B's
        // refinement initial state.
        //

        Iterator Transitions = model.relationList().iterator();
        while (Transitions.hasNext()) {
            Relation transition = (Relation) Transitions.next();
            if (!(transition instanceof Transition)) {
                continue;
            }
            State source = ((Transition) transition).sourceState();
            State destination = ((Transition) transition).destinationState();

            // If the source state has refinement, then every state in the
            // refinement must have a state which connects to the destination;
            // if the destination has refinements, then all of these newly added
            // transitions must be connected to the refinement initial state.
            TypedActor[] sActors = source.getRefinement();
            TypedActor[] dActors = destination.getRefinement();
            if (sActors == null && dActors == null) {

                // We only need to find the corresponding node in the
                // system and set up a connection for that.

                Iterator returnFSMActorStates = returnFSMActor.entityList()
                        .iterator();
                State sCorresponding = null;
                State dCorresponding = null;
                while (returnFSMActorStates.hasNext()) {
                    NamedObj cState = (NamedObj) returnFSMActorStates.next();
                    if (cState instanceof State) {
                        if (((State) cState).getName().equalsIgnoreCase(
                                source.getName().trim())) {

                            sCorresponding = (State) cState;
                        }
                    }
                }
                returnFSMActorStates = returnFSMActor.entityList().iterator();
                while (returnFSMActorStates.hasNext()) {
                    NamedObj cState = (NamedObj) returnFSMActorStates.next();
                    if (cState instanceof State) {
                        if (((State) cState).getName().equalsIgnoreCase(
                                destination.getName().trim())) {
                            dCorresponding = (State) cState;

                        }
                    }
                }

                Port s = sCorresponding.outgoingPort;
                Port d = dCorresponding.incomingPort;
                Transition newTransition = (Transition) transition.clone();
                newTransition.unlinkAll();
                newTransition.setContainer(returnFSMActor);
                newTransition.moveToFirst();
                s.link(newTransition);
                d.link(newTransition);
                newTransition.setName(source.getName().trim() + "-"
                        + destination.getName().trim());

            } else if (sActors == null && dActors != null) {
                // We need to retrieve the source and connect with
                // the inner initial state of destination.

                // First retrieve the inner model initial state of
                // destination.
                TypedActor dInnerActor = dActors[0];
                if (!(dInnerActor instanceof FSMActor)) {
                    // This is currently beyond our scope.
                    throw new IllegalActionException(
                            "REDUtility._rewriteModalModelWithStateRefinementToFSMActor() clashes:\n"
                                    + "Beyond the scope for processing");
                }

                Iterator returnFSMActorStates = returnFSMActor.entityList()
                        .iterator();
                State sCorresponding = null;
                State dCorresponding = null;
                while (returnFSMActorStates.hasNext()) {
                    NamedObj cState = (NamedObj) returnFSMActorStates.next();
                    if (cState instanceof State) {
                        if (((State) cState).getName().equalsIgnoreCase(
                                source.getName().trim())) {

                            sCorresponding = (State) cState;
                        }
                    }
                }
                returnFSMActorStates = returnFSMActor.entityList().iterator();
                while (returnFSMActorStates.hasNext()) {
                    NamedObj cState = (NamedObj) returnFSMActorStates.next();
                    if (cState instanceof State) {
                        if (((State) cState).getName().equalsIgnoreCase(
                                destination.getName().trim()
                                + "-"
                                + ((FSMActor) dInnerActor)
                                .getInitialState().getName()
                                .trim())) {
                            dCorresponding = (State) cState;
                        }
                    }
                }

                Port s = sCorresponding.outgoingPort;
                Port d = dCorresponding.incomingPort;
                Transition newTransition = (Transition) transition.clone();
                newTransition.unlinkAll();
                newTransition.setContainer(returnFSMActor);
                newTransition.moveToFirst();
                s.link(newTransition);
                d.link(newTransition);
                newTransition.setName(source.getName().trim()
                        + "-"
                        + destination.getName().trim()
                        + "-"
                        + ((FSMActor) dInnerActor).getInitialState().getName()
                        .trim());

            } else if (sActors != null && dActors == null) {
                // We need to connect every inner state in the source and
                // with destination state. We may copy existing transitions
                // from the upper layer and modify it.

                TypedActor innerActor = sActors[0];
                if (innerActor instanceof FSMActor) {

                    Iterator innerStates = ((FSMActor) innerActor).entityList()
                            .iterator();
                    while (innerStates.hasNext()) {
                        NamedObj innerState = (NamedObj) innerStates.next();
                        if (innerState instanceof State) {

                            Iterator returnFSMActorStates = returnFSMActor
                                    .entityList().iterator();
                            State sCorresponding = null;
                            State dCorresponding = null;
                            while (returnFSMActorStates.hasNext()) {
                                NamedObj cState = (NamedObj) returnFSMActorStates
                                        .next();
                                if (cState instanceof State) {
                                    if (((State) cState).getName()
                                            .equalsIgnoreCase(
                                                    source.getName().trim()
                                                    + "-"
                                                    + innerState
                                                    .getName()
                                                    .trim())) {

                                        sCorresponding = (State) cState;
                                    }
                                }
                            }
                            returnFSMActorStates = returnFSMActor.entityList()
                                    .iterator();
                            while (returnFSMActorStates.hasNext()) {
                                NamedObj cState = (NamedObj) returnFSMActorStates
                                        .next();
                                if (cState instanceof State) {
                                    if (((State) cState).getName()
                                            .equalsIgnoreCase(
                                                    destination.getName()
                                                    .trim())) {
                                        dCorresponding = (State) cState;
                                    }
                                }
                            }

                            Port s = sCorresponding.outgoingPort;
                            Port d = dCorresponding.incomingPort;
                            Transition newTransition = (Transition) transition
                                    .clone();
                            newTransition.unlinkAll();
                            newTransition.setContainer(returnFSMActor);
                            newTransition.moveToFirst();
                            s.link(newTransition);
                            d.link(newTransition);
                            newTransition.setName(source.getName().trim() + "-"
                                    + innerState.getName().trim() + "-"
                                    + destination.getName().trim());
                        }
                    }
                }

            } else {
                // Do the combination of the previous two cases.
                // First retrieve the inner initial state

                TypedActor sInnerActor = null;
                TypedActor dInnerActor = null;
                if (sActors != null && sActors[0] != null) {
                    sInnerActor = sActors[0];
                }
                if (dActors != null && dActors[0] != null) {
                    dInnerActor = dActors[0];
                }

                String newDestName = "";
                if (dInnerActor instanceof FSMActor) {
                    newDestName = destination.getName().trim()
                            + "-"
                            + ((FSMActor) dInnerActor).getInitialState()
                            .getName().trim();
                }
                if (sInnerActor instanceof FSMActor) {
                    Iterator innerStates = ((FSMActor) sInnerActor)
                            .entityList().iterator();
                    while (innerStates.hasNext()) {
                        NamedObj innerState = (NamedObj) innerStates.next();
                        if (innerState instanceof State) {
                            // Retrieve the name, generate a new transition

                            Transition newTransition = (Transition) transition
                                    .clone(model.workspace());
                            newTransition.unlinkAll();
                            // Find the corresponding State in the
                            // returnFSMActor
                            Iterator returnFSMActorStates = returnFSMActor
                                    .entityList().iterator();
                            State sCorresponding = null;
                            State dCorresponding = null;
                            while (returnFSMActorStates.hasNext()) {
                                NamedObj cState = (NamedObj) returnFSMActorStates
                                        .next();
                                if (cState instanceof State) {
                                    if (((State) cState).getName()
                                            .equalsIgnoreCase(
                                                    source.getName().trim()
                                                    + "-"
                                                    + innerState
                                                    .getName()
                                                    .trim())) {

                                        sCorresponding = (State) cState;
                                    }
                                }
                            }
                            returnFSMActorStates = returnFSMActor.entityList()
                                    .iterator();
                            while (returnFSMActorStates.hasNext()) {
                                NamedObj cState = (NamedObj) returnFSMActorStates
                                        .next();
                                if (cState instanceof State) {
                                    if (((State) cState).getName()
                                            .equalsIgnoreCase(newDestName)) {
                                        dCorresponding = (State) cState;

                                    }
                                }
                            }

                            Port s = sCorresponding.outgoingPort;
                            Port d = dCorresponding.incomingPort;
                            newTransition.unlinkAll();
                            newTransition.setContainer(returnFSMActor);
                            newTransition.moveToFirst();
                            s.link(newTransition);
                            d.link(newTransition);
                            newTransition.setName(source.getName().trim() + "-"
                                    + innerState.getName().trim() + "-"
                                    + newDestName);

                        }
                    }
                }
            }
        }
        return returnFSMActor;
    }

    /**
     * Translate a bounded buffer, nondeterministic delayed actor.
     * @param delayedActor The actor.
     * @param inputSignalName The input signal
     * @param outputSignalName The output signal
     * @return description of the TimedDelayActor acceptable by model checker
     *         RED.
     * @exception IllegalActionException If the <i>delay</i> or
     * <i>bufferSize</i> parameters of the delayedActor cannot be read.
     */
    private static REDSingleEntityBean _translateBBNondeterministicDelayedActor(
            BoundedBufferNondeterministicDelay delayedActor,
            String inputSignalName, String outputSignalName)
                    throws IllegalActionException {

        // If we expect to convert a TimedDelayedActor into a timed automata,
        // we need to have the following information from the
        // BoundedBufferTimedDelay actor:
        // (1) delay time:
        // (2) size of buffer:

        // Also, we need to retrieve the information for the name of
        // input and output signals. These (inputSignalName, outputSignalName)
        // should be analyzed by callers and pass to this function.

        // Retrieve parameters from clockActors
        double delay = ((DoubleToken) delayedActor.delay.getToken())
                .doubleValue();
        int bufferSize = ((IntToken) delayedActor.bufferSize.getToken())
                .intValue();

        REDSingleEntityBean bean = new REDSingleEntityBean();

        bean._defineConstants.append("#define " + delayedActor.getName().trim()
                + "_DELAY " + String.valueOf((int) delay) + "\n");

        REDModuleNameInitialBean innerBean = new REDModuleNameInitialBean();
        innerBean._name = delayedActor.getName().trim();
        StringBuffer str = new StringBuffer(delayedActor.getName().trim()
                + "_S");
        for (int i = 0; i < bufferSize; i++) {
            str.append("0");
        }
        innerBean._initialStateDescription = str.toString();
        bean._nameInitialState = innerBean;
        // Based on different buffer size, we need to generate the delayedActor
        // which represents the buffer situation.
        // For a delayedActor with buffer size 1, the we need two states:
        // delayedActor_0 to represent no token.
        // delayedActor_1 to represent one token.
        //
        // Also, clocks used by the system is related to the size of the buffer.
        // We need to capture this behavior.
        //
        // Decide number of clocks used in the system.
        for (int i = 0; i < bufferSize; i++) {
            bean._clockSet.add(delayedActor.getName().trim() + "_C"
                    + String.valueOf(i));

        }

        // Generate strings with arbitrary combination of 0 and 1s of fixed
        // length bufferSize. For the case of buffer size is 3,
        // then we generate 000, 001, 010, 011, 100, 101, 110, 111.
        // These separate numbers would represent different states
        // indicating the status of buffers.

        ArrayList<String> initial = new ArrayList<String>();
        initial.add("");
        ArrayList<String> stringList = _enumerateString(bufferSize, initial);

        // Generate the state based on the content in the stringList
        bean._moduleDescription.append("\n/* Process name: "
                + delayedActor.getName().trim() + " */\n");
        for (String content : stringList) {

            bean._moduleDescription.append("mode "
                    + delayedActor.getName().trim() + "_S" + content.trim()
                    + " (");
            char[] charContent = content.toCharArray();

            StringBuffer StateClockConstraint = new StringBuffer("");
            StringBuffer StateTransitionCondition = new StringBuffer("");
            boolean clockAssigned = false;

            if (charContent.length != 0) {
                for (int i = charContent.length - 1; i >= 0; i--) {
                    if (charContent[i] == '0') {
                        if (clockAssigned == false) {
                            char[] newStateContent = content.toCharArray();
                            newStateContent[i] = '1';
                            StateTransitionCondition.append("    when ?"
                                    + inputSignalName.trim() + " (true) may "
                                    + delayedActor.getName().trim() + "_C"
                                    + String.valueOf(i) + " = 0 ; goto "
                                    + delayedActor.getName().trim() + "_S"
                                    + String.valueOf(newStateContent) + "; \n");
                            clockAssigned = true;
                        }
                    } else {
                        if (StateClockConstraint.toString()
                                .equalsIgnoreCase("")) {
                            StateClockConstraint
                            .append(" " + delayedActor.getName().trim()
                                    + "_C" + String.valueOf(i) + "<= "
                                    + delayedActor.getName().trim()
                                    + "_DELAY ");
                        } else {
                            StateClockConstraint
                            .append(" && "
                                    + delayedActor.getName().trim()
                                    + "_C" + String.valueOf(i) + "<= "
                                    + delayedActor.getName().trim()
                                    + "_DELAY ");
                        }
                        char[] newStateContent = content.toCharArray();
                        newStateContent[i] = '0';
                        StateTransitionCondition.append("    when " + "!"
                                + outputSignalName.trim() + " ("
                                + delayedActor.getName().trim() + "_C"
                                + String.valueOf(i) + " <= "
                                + delayedActor.getName().trim() + "_DELAY && "
                                + delayedActor.getName().trim() + "_C"
                                + String.valueOf(i) + ">0) may goto "
                                + delayedActor.getName().trim() + "_S"
                                + String.valueOf(newStateContent) + "; \n");
                        if (i == 0) {
                            if (content.contains("0") == false) {
                                // All true cases. then we need to represent one
                                // case for overflow.
                                StateTransitionCondition
                                .append("    when ?"
                                        + outputSignalName.trim()
                                        + " (true) may goto Buffer_Overflow; \n");
                            }
                        }
                    }
                }
                if (StateClockConstraint.toString().trim().equalsIgnoreCase("")) {
                    bean._moduleDescription.append("true ) { \n");
                } else {
                    bean._moduleDescription.append(StateClockConstraint
                            .toString() + " ) { \n");
                }
                bean._moduleDescription.append(StateTransitionCondition);
                bean._moduleDescription.append("}\n");
            }
        }
        return bean;
    }

    /**
     * This is an utility function which performs the translation of a single
     * TimedDelay actor into the format of communicating timed automata (CTA)
     * acceptable by model checker RED.
     *
     * @param delayedActor actor which needs to be converted
     * @param inputSignalName The name of the input signal. This must be derived externally.
     * @param outputSignalName
     *                The name of the output signal. This must be derived
     *                externally.
     * @return description of the TimedDelayActor acceptable by model checker
     *         RED.
     * @exception IllegalActionException
     */
    private static REDSingleEntityBean _translateBBTimedDelayedActor(
            BoundedBufferTimedDelay delayedActor, String inputSignalName,
            String outputSignalName) throws IllegalActionException {

        // If we expect to convert a BoundedBufferTimedDelayedActor into a timed
        // automata,
        // we need to have the following information from the
        // BoundedBufferTimedDelay actor:
        // (1) delay time:
        // (2) size of buffer:
        //
        // Also, we need to retrieve the information for the name of
        // input and output signals. These (inputSignalName, outputSignalName)
        // should be analyzed by callers and pass to this function.
        //

        // Retrieve parameters from clockActors
        double delay = ((DoubleToken) delayedActor.delay.getToken())
                .doubleValue();
        int bufferSize = ((IntToken) delayedActor.bufferSize.getToken())
                .intValue();

        REDSingleEntityBean bean = new REDSingleEntityBean();

        bean._defineConstants.append("#define " + delayedActor.getName().trim()
                + "_DELAY " + String.valueOf((int) delay) + "\n");

        REDModuleNameInitialBean innerBean = new REDModuleNameInitialBean();
        innerBean._name = delayedActor.getName().trim();
        // innerBean._initialStateDescription =
        StringBuffer str = new StringBuffer(delayedActor.getName().trim()
                + "_S");
        for (int i = 0; i < bufferSize; i++) {
            str.append("0");
        }
        innerBean._initialStateDescription = str.toString();
        bean._nameInitialState = innerBean;

        // Based on different buffer size, we need to generate the delayedActor
        // which represents the buffer situation.
        // For a delayedActor with buffer size 1, the we need two states:
        // delayedActor_0 to represent no token.
        // delayedActor_1 to represent one token.
        //
        // Also, clocks used by the system is related to the size of the buffer.
        // We need to capture this behavior.
        //
        // Decide number of clocks used in the system.
        for (int i = 0; i < bufferSize; i++) {
            bean._clockSet.add(delayedActor.getName().trim() + "_C"
                    + String.valueOf(i));

        }

        // Generate strings with arbitrary combination of 0 and 1s of fixed
        // length bufferSize. For the case of buffer size is 3,
        // then we generate 000, 001, 010, 011, 100, 101, 110, 111.
        // These separate numbers would represent different states
        // indicating the status of buffers.

        ArrayList<String> initial = new ArrayList<String>();
        initial.add("");
        ArrayList<String> stringList = _enumerateString(bufferSize, initial);

        // Generate the state based on the content in the stringList
        bean._moduleDescription.append("\n/* Process name: "
                + delayedActor.getName().trim() + " */\n");
        for (String content : stringList) {

            bean._moduleDescription.append("mode "
                    + delayedActor.getName().trim() + "_S" + content.trim()
                    + " (");
            char[] charContent = content.toCharArray();

            StringBuffer StateClockConstraint = new StringBuffer("");
            StringBuffer StateTransitionCondition = new StringBuffer("");
            boolean clockAssigned = false;

            if (charContent.length != 0) {
                for (int i = charContent.length - 1; i >= 0; i--) {
                    if (charContent[i] == '0') {

                        // We don't need to add clock constraints.

                        // when ?inputSignal(TimedDelay2_Ci <=
                        // TIMEDDELAY2_DELAY) may TimedDelay2_Ci = 0; goto
                        // TimedDelay2_SXX1;
                        //
                        // Note that we are not setting all clocks.
                        // Instead, we would set up only one clock.
                        // For example, in 1(C0)0(C1)0(C2), we would only set up
                        // C2.
                        // In 0(C0)0(C1)1(C2), we would only set up C1, but not
                        // C0.
                        if (clockAssigned == false) {
                            char[] newStateContent = content.toCharArray();

                            newStateContent[i] = '1';
                            StateTransitionCondition.append("    when ?"
                                    + inputSignalName.trim() + " (true) may "
                                    + delayedActor.getName().trim() + "_C"
                                    + String.valueOf(i) + " = 0 ; goto "
                                    + delayedActor.getName().trim() + "_S"
                                    + String.valueOf(newStateContent) + "; \n");
                            clockAssigned = true;
                        }

                    } else {
                        // (charContent[i] == '1')
                        // We need to add up clock constraints
                        if (StateClockConstraint.toString()
                                .equalsIgnoreCase("")) {
                            StateClockConstraint
                            .append(" " + delayedActor.getName().trim()
                                    + "_C" + String.valueOf(i) + "<= "
                                    + delayedActor.getName().trim()
                                    + "_DELAY ");
                        } else {
                            StateClockConstraint
                            .append(" && "
                                    + delayedActor.getName().trim()
                                    + "_C" + String.valueOf(i) + "<= "
                                    + delayedActor.getName().trim()
                                    + "_DELAY ");
                        }

                        // when !D_Pgo (TimedDelay2_C1 == TIMEDDELAY2_DELAY) may
                        // goto TimedDelay2_S00;
                        char[] newStateContent = content.toCharArray();
                        newStateContent[i] = '0';
                        StateTransitionCondition.append("    when " + "!"
                                + outputSignalName.trim() + " ("
                                + delayedActor.getName().trim() + "_C"
                                + String.valueOf(i) + " == "
                                + delayedActor.getName().trim()
                                + "_DELAY ) may goto "
                                + delayedActor.getName().trim() + "_S"
                                + String.valueOf(newStateContent) + "; \n");
                        if (i == 0) {
                            if (content.contains("0") == false) {
                                // All true cases. then we need to represent one
                                // case
                                // for overflow.
                                StateTransitionCondition
                                .append("    when ?"
                                        + outputSignalName.trim()
                                        + " (true) may goto Buffer_Overflow; \n");
                            }
                        }

                    }
                }
                if (StateClockConstraint.toString().trim().equalsIgnoreCase("")) {
                    bean._moduleDescription.append("true ) { \n");
                } else {
                    bean._moduleDescription.append(StateClockConstraint
                            .toString() + " ) { \n");
                }

                bean._moduleDescription.append(StateTransitionCondition);
                bean._moduleDescription.append("}\n");

            }

        }

        return bean;
    }

    /**
     * This is an utility function which performs the translation of a single
     * clock actor into the format of communicating timed automata (CTA)
     * acceptable by model checker RED.
     *
     * @param clockActor The actor which requires to be converted.
     * @param outputSignalName The name of the output signal. This must be derived externally.
     * @return clock description acceptable by model checker RED.
     * @exception IllegalActionException
     */
    private static REDSingleEntityBean _translateClockActor(Clock clockActor,
            String outputSignalName) throws IllegalActionException {

        // REDSingleEntityBean returnBean = new REDSingleEntityBean();

        // If we expect to convert a clock into a timed automata,
        // we need to have the following information from the clockActor:
        // (1) period:
        // (2) numberOfCycles: UNBOUNDED means no limit on it.
        //     If it is not unbounded, we need to set up a counter
        //     to set the number of emit signals
        // (3) stopTime: Infinity means the clock would not stop
        //
        // Also, we need to retrieve the information for the name of
        // the signal which the receiver receives.
        // This (outputSignalName) should be analyzed by callers and
        // pass to this function.

        // Retrieve parameters from clockActors
        double period = ((DoubleToken) clockActor.period.getToken())
                .doubleValue();
        String numberOfCycles = ((IntToken) clockActor.numberOfCycles
                .getToken()).toString();
        double stopTime = ((DoubleToken) clockActor.stopTime.getToken())
                .doubleValue();
        String sStopTime = String.valueOf(stopTime);

        if (numberOfCycles.equalsIgnoreCase("-1")
                || numberOfCycles.equalsIgnoreCase("UNBOUNDED")) {
            // For unbounded cases, it's easier. If the number of cycles
            // not unbounded, we need to have a counter to count the number
            // of cycles it consumes.
            if (sStopTime.equalsIgnoreCase("Infinity")) {

                REDSingleEntityBean bean = new REDSingleEntityBean();
                bean._defineConstants.append("#define "
                        + clockActor.getName().trim() + "_PERIOD "
                        + String.valueOf((int) period / 2) + "\n");
                bean._clockSet.add(clockActor.getName().trim() + "_C1");

                bean._moduleDescription.append("\n/* Process name: "
                        + clockActor.getName().trim() + " */\n");
                bean._moduleDescription.append("mode "
                        + clockActor.getName().trim() + "_init ("
                        + clockActor.getName().trim() + "_C1 == 0) { \n");
                bean._moduleDescription.append("    when " + "!"
                        + outputSignalName.trim() + " ("
                        + clockActor.getName().trim() + "_C1 == 0) may "
                        + clockActor.getName().trim() + "_C1 = 0 ; goto "
                        + clockActor.getName().trim() + "_state;" + "\n");
                bean._moduleDescription.append("}\n");
                bean._moduleDescription.append("mode "
                        + clockActor.getName().trim() + "_state ("
                        + clockActor.getName().trim() + "_C1 <= "
                        + clockActor.getName().trim() + "_PERIOD) { \n");
                bean._moduleDescription.append("    when " + "!"
                        + outputSignalName.trim() + " ("
                        + clockActor.getName().trim() + "_C1 == "
                        + clockActor.getName().trim() + "_PERIOD) may "
                        + clockActor.getName().trim() + "_C1 = 0 ; \n");
                bean._moduleDescription.append("}\n");

                REDModuleNameInitialBean innerBean = new REDModuleNameInitialBean();
                innerBean._name = clockActor.getName().trim();
                innerBean._initialStateDescription = clockActor.getName()
                        .trim() + "_init";
                bean._nameInitialState = innerBean;
                return bean;

            } else {
                REDSingleEntityBean bean = new REDSingleEntityBean();
                bean._defineConstants.append("#define "
                        + clockActor.getName().trim() + "_PERIOD "
                        + String.valueOf((int) period / 2) + " \n");
                bean._defineConstants.append("#define "
                        + clockActor.getName().trim() + "_STOP_TIME "
                        + String.valueOf(stopTime) + "\n");
                bean._clockSet.add(clockActor.getName().trim() + "_C1");
                bean._clockSet.add(clockActor.getName().trim() + "_C2");

                bean._moduleDescription.append("/* Process name: "
                        + clockActor.getName().trim() + " */\n");
                bean._moduleDescription.append("mode "
                        + clockActor.getName().trim() + "_init ("
                        + clockActor.getName().trim() + "_C1 == 0"
                        + clockActor.getName().trim() + "_C2 <= "
                        + clockActor.getName().trim() + "_STOPTIME" + ") { \n");
                bean._moduleDescription.append("    when " + "!"
                        + outputSignalName.trim() + " ("
                        + clockActor.getName().trim() + "_C1 == 0) may "
                        + clockActor.getName().trim() + "_C1 = 0 ; goto "
                        + clockActor.getName().trim() + "_state; \n");
                bean._moduleDescription.append("}\n");
                bean._moduleDescription.append("mode "
                        + clockActor.getName().trim() + "_state ("
                        + clockActor.getName().trim() + "_C1 <= "
                        + clockActor.getName().trim() + "_PERIOD"
                        + clockActor.getName().trim() + "_C2 <= "
                        + clockActor.getName().trim() + "_STOPTIME" + ") { \n");
                bean._moduleDescription.append("    when " + "!"
                        + outputSignalName.trim() + " ("
                        + clockActor.getName().trim() + "_C1 == "
                        + clockActor.getName().trim() + "_PERIOD) may "
                        + clockActor.getName().trim() + "_C1 = 0 ;  \n");
                bean._moduleDescription.append("    when ("
                        + clockActor.getName().trim() + "_C2 == "
                        + clockActor.getName().trim() + "_STOPTIME) may goto "
                        + clockActor.getName().trim() + "_idle; \n");
                bean._moduleDescription.append("}\n");
                bean._moduleDescription.append("mode "
                        + clockActor.getName().trim() + "_idle (true) { \n");
                bean._moduleDescription.append("}\n");

                REDModuleNameInitialBean innerBean = new REDModuleNameInitialBean();
                innerBean._name = clockActor.getName().trim();
                innerBean._initialStateDescription = clockActor.getName()
                        .trim() + "_init";
                bean._nameInitialState = innerBean;

                return bean;

            }
        } else {
            // We need to count on number of cycles.
            // We use a integer number to store the number of cycles
            // When the system reaches the number, it would go to idle
            // state.
            if (sStopTime.equalsIgnoreCase("Infinity")) {
                REDSingleEntityBean bean = new REDSingleEntityBean();
                bean._defineConstants.append("#define "
                        + clockActor.getName().trim() + "_PERIOD "
                        + String.valueOf((int) period / 2) + " \n");
                bean._defineConstants.append("#define "
                        + clockActor.getName().trim() + "_STOP_CYCLE_COUNT "
                        + String.valueOf(numberOfCycles) + "\n");
                bean._declaredVariables.append("global discrete "
                        + clockActor.getName().trim() + "_Cycle" + ":0.."
                        + numberOfCycles + "; \n");
                bean._clockSet.add(clockActor.getName().trim() + "_C1");

                bean._moduleDescription.append("/* Process name: "
                        + clockActor.getName().trim() + " */\n");
                bean._moduleDescription.append("mode "
                        + clockActor.getName().trim() + "_init ("
                        + clockActor.getName().trim() + "_C1 == 0) { \n");
                bean._moduleDescription
                .append("    when " + "!" + outputSignalName.trim()
                        + " (" + clockActor.getName().trim()
                        + "_C1 == 0 &&" + clockActor.getName().trim()
                        + "_Cycle < " + clockActor.getName().trim()
                        + "_STOP_CYCLE_COUNT" + ") may "
                        + clockActor.getName().trim() + "_C1 = 0 ; "
                        + clockActor.getName().trim() + "_Cycle = "
                        + clockActor.getName().trim() + "_Cycle + 1;"
                        + " goto " + clockActor.getName().trim()
                        + "_state; \n");
                bean._moduleDescription.append("    when  ("
                        + clockActor.getName().trim() + "_Cycle == "
                        + clockActor.getName().trim()
                        + "_STOP_CYCLE_COUNT) may goto "
                        + clockActor.getName().trim() + "_idle; \n");
                bean._moduleDescription.append("}\n");

                bean._moduleDescription.append("mode "
                        + clockActor.getName().trim() + "_state ("
                        + clockActor.getName().trim() + "_C1 <= "
                        + clockActor.getName().trim() + "_PERIOD" + ") { \n");
                bean._moduleDescription.append("    when " + "!"
                        + outputSignalName.trim() + " ("
                        + clockActor.getName().trim() + "_C1 == "
                        + clockActor.getName().trim() + "_PERIOD &&"
                        + clockActor.getName().trim() + "_Cycle < "
                        + clockActor.getName().trim() + "_STOP_CYCLE_COUNT"
                        + ") may " + clockActor.getName().trim() + "_C1 = 0 ; "
                        + clockActor.getName().trim() + "_Cycle = "
                        + clockActor.getName().trim() + "_Cycle + 1;" + " \n");
                bean._moduleDescription.append("    when  ("
                        + clockActor.getName().trim() + "_Cycle == "
                        + clockActor.getName().trim()
                        + "_STOP_CYCLE_COUNT) may goto "
                        + clockActor.getName().trim() + "_idle; \n");
                bean._moduleDescription.append("}\n");
                bean._moduleDescription.append("mode "
                        + clockActor.getName().trim() + "_idle (true) { \n");
                bean._moduleDescription.append("}\n");

                REDModuleNameInitialBean innerBean = new REDModuleNameInitialBean();
                innerBean._name = clockActor.getName().trim();
                innerBean._initialStateDescription = clockActor.getName()
                        .trim() + "_init";
                bean._nameInitialState = innerBean;

                bean._variableInitialDescriptionSet.add(clockActor.getName()
                        .trim() + "_Cycle == 0" + " ");
                return bean;
            } else {
                REDSingleEntityBean bean = new REDSingleEntityBean();
                bean._defineConstants.append("#define "
                        + clockActor.getName().trim() + "_PERIOD "
                        + String.valueOf((int) period / 2) + " \n");
                bean._defineConstants.append("#define "
                        + clockActor.getName().trim() + "_STOP_CYCLE_COUNT "
                        + String.valueOf(numberOfCycles) + "\n");
                bean._defineConstants.append("#define "
                        + clockActor.getName().trim() + "_STOP_TIME "
                        + String.valueOf(stopTime) + "\n");

                bean._declaredVariables.append("global discrete "
                        + clockActor.getName().trim() + "_Cycle" + ":0.."
                        + numberOfCycles + "; \n");
                bean._clockSet.add(clockActor.getName().trim() + "_C1");
                bean._clockSet.add(clockActor.getName().trim() + "_C2");

                bean._moduleDescription.append("/* Process name: "
                        + clockActor.getName().trim() + " */\n");
                bean._moduleDescription.append("mode "
                        + clockActor.getName().trim() + "_init ("
                        + clockActor.getName().trim() + "_C1 <= "
                        + clockActor.getName().trim() + "_PERIOD" + ") { \n");
                bean._moduleDescription.append("    when " + "!"
                        + outputSignalName.trim() + " ("
                        + clockActor.getName().trim() + "_C1 == 0 &&"
                        + clockActor.getName().trim() + "_Cycle < "
                        + clockActor.getName().trim() + "_STOP_CYCLE_COUNT"
                        + ") may " + clockActor.getName().trim() + "_C1 = 0 ; "
                        + clockActor.getName().trim() + "_Cycle = "
                        + clockActor.getName().trim() + "_Cycle + 1; goto "
                        + clockActor.getName().trim() + "_state; \n");
                bean._moduleDescription.append("    when  ("
                        + clockActor.getName().trim() + "_Cycle == "
                        + clockActor.getName().trim()
                        + "_STOP_CYCLE_COUNT) may goto "
                        + clockActor.getName().trim() + "_idle; \n");
                bean._moduleDescription.append("    when ("
                        + clockActor.getName().trim() + "_C2 == "
                        + clockActor.getName().trim() + "_STOPTIME) may goto "
                        + clockActor.getName().trim() + "_idle; \n");
                bean._moduleDescription.append("}\n");

                bean._moduleDescription.append("mode "
                        + clockActor.getName().trim() + "_state ("
                        + clockActor.getName().trim() + "_C1 <= "
                        + clockActor.getName().trim() + "_PERIOD" + ") { \n");
                bean._moduleDescription.append("    when " + "!"
                        + outputSignalName.trim() + " ("
                        + clockActor.getName().trim() + "_C1 == "
                        + clockActor.getName().trim() + "_PERIOD &&"
                        + clockActor.getName().trim() + "_Cycle < "
                        + clockActor.getName().trim() + "_STOP_CYCLE_COUNT"
                        + ") may " + clockActor.getName().trim() + "_C1 = 0 ; "
                        + clockActor.getName().trim() + "_Cycle = "
                        + clockActor.getName().trim() + "_Cycle + 1;" + " \n");
                bean._moduleDescription.append("    when  ("
                        + clockActor.getName().trim() + "_Cycle == "
                        + clockActor.getName().trim()
                        + "_STOP_CYCLE_COUNT) may goto "
                        + clockActor.getName().trim() + "_idle; \n");
                bean._moduleDescription.append("    when ("
                        + clockActor.getName().trim() + "_C2 == "
                        + clockActor.getName().trim() + "_STOPTIME) may goto "
                        + clockActor.getName().trim() + "_idle; \n");
                bean._moduleDescription.append("}\n");
                bean._moduleDescription.append("mode "
                        + clockActor.getName().trim() + "_idle (true) { \n");
                bean._moduleDescription.append("}\n");

                REDModuleNameInitialBean innerBean = new REDModuleNameInitialBean();
                innerBean._name = clockActor.getName().trim();
                innerBean._initialStateDescription = clockActor.getName()
                        .trim() + "_init";
                bean._nameInitialState = innerBean;

                bean._variableInitialDescriptionSet.add(clockActor.getName()
                        .trim() + "_Cycle == 0" + " ");
                return bean;
            }
        }
    }

    /**
     * Translate an FSM actor.
     * @param actor The actor to be translated.
     * @param span The size of the span to expand the variable domain. This
     * variable is used to determine the inner variables used by an actor
     * @param globalSynchronizerSet The set of useful
     * synchronizers. There are some synchronizers which is not
     * useful. They are not connected to/from a valid actor where
     * analysis is possible.
     * @return description of the TimedDelayActor acceptable by model checker
     * RED.
     * @exception IllegalActionException If thrown while getting the initial
     * state of the actor, deciding the guard signal variable set or
     * generating transitions.
     */
    private static REDSingleEntityBean _translateFSMActor(FSMActor actor,
            int span, HashSet<String> globalSynchronizerSet)
                    throws IllegalActionException {

        REDSingleEntityBean bean = new REDSingleEntityBean();
        bean._moduleDescription.append("\n/* Process name: "
                + actor.getName().trim() + " */\n");

        REDModuleNameInitialBean moduleNameInitialState = new REDModuleNameInitialBean();
        moduleNameInitialState._name = actor.getName();
        moduleNameInitialState._initialStateDescription = actor.getName()
                + "_State_" + actor.getInitialState().getName().trim()
                + "_Plum";
        bean._nameInitialState = moduleNameInitialState;

        // Determine the number of ports required and specify their names.
        // Note that some input signals might be useless; but if there exists
        // a transition which has the guard "true", then it should listen to
        // all input ports (when any input port receives a token, the transition
        // can be enabled).
        HashSet<String> guardSignalSet = _decideGuardSignalVariableSet(actor);
        Iterator<String> it = guardSignalSet.iterator();
        while (it.hasNext()) {
            String signalName = it.next();
            // Add this signal to the bean.
            REDModuleNameInitialBean nameInitialBean = new REDModuleNameInitialBean();
            nameInitialBean._name = actor.getName().trim() + "_Port_"
                    + signalName.trim();
            nameInitialBean._initialStateDescription = actor.getName().trim()
                    + "_Port_" + signalName.trim() + "_TokenEmpty";
            bean._portSet.add(nameInitialBean);

            // Empty means that the port does not contain any token; mode Port_TokenEmpty (true) {
            bean._moduleDescription.append("mode " + actor.getName().trim()
                    + "_Port_" + signalName.trim() + "_TokenEmpty"
                    + " ( true ) { \n");
            // All possible transitions in TokenEmpty are described as follows.
            // (1) The token is received, and simultaneously it is consumed with the transition
            //     inside the FSM. In this way, it will remain empty status.
            // (2) The token is received, and it is not consumed by the transition. In this way,
            //     the system moves to the location "TokenOccupied".
            // signalName.trim(): Signal from outside sending
            // ND_signalName.trim(): Signal for the internal consumption of the FSMActor
            // Note that all guard signal should be renamed as ND_signalName
            // ND stands for non-delayed signals

            bean._moduleDescription.append("    when ?" + signalName.trim()
                    + " !ND_" + signalName.trim() + " (true) may ; \n");
            bean._moduleDescription.append("    when ?" + signalName.trim()
                    + " (true) may t = 0; goto " + actor.getName().trim()
                    + "_Port_" + signalName.trim() + "_TokenOccupied" + ";\n");
            bean._moduleDescription.append("} \n");

            // Occupied means that the port contains the token; mode Port_TokenOccupied (t==0) {
            bean._moduleDescription.append("mode " + actor.getName().trim()
                    + "_Port_" + signalName.trim() + "_TokenOccupied"
                    + " ( t==0 ) { \n");
            // All possible transitions in TokenOccupied are described as follows.
            // (1) When another token is received, it remains in its state.
            // (2) When a transition consumes the token, it moves back to empty.
            // (3) When time elapses, it moves back to empty.

            bean._moduleDescription.append("    when !ND_" + signalName.trim()
                    + " (true) may ; goto " + actor.getName().trim() + "_Port_"
                    + signalName.trim() + "_TokenEmpty" + ";\n");
            bean._moduleDescription.append("    when  ?" + signalName.trim()
                    + " (true) may ; \n");

            bean._moduleDescription.append("/*    when  (t>=0) may  goto "
                    + actor.getName().trim() + "_Port_" + signalName.trim()
                    + "_TokenEmpty" + "; */\n");

            // Here an additional transition is added to replace the above
            // transition commented.
            //
            // This transition is used to represent the case when a token
            // is received in the port, but the FSMActor can not perform
            // any action. In this way, in the semantics the system
            // should not do anything and let the time pass to the next
            // instant. When time passes, the token should simultaneously
            // disappear.
            //
            // To have this feature, we have to enforce the FSMActor
            // to perform a "still move" if no possible actions can be taken.
            // We discuss all possible cases:
            //        Case 1: when token is available, and the FSMActor can take the
            //                transition with the token, the port automata first moves
            //                itself to occupied location (with invariant 0), and
            //                performs the consumption process.
            //        Case 2: when token is available, but the FSMActor can not take
            //                any action, the port automata first moves itself to
            //                occupied location (with invariant 0), and performs the
            //                consumption process accompanied by the "still move" transition.
            //
            // Note that the still move transition should be designed such that
            // the guard condition is the negation of "disjunction". For example,
            // if we have two ports: Sec and Test.
            // If a transition is of the following format :
            // "if (Sec_isPresent && count < 6) then ..."
            // Then we add a still move using TokenTestConsume! synchronizer to the
            // FSMActor: with the condition (TokenTestConsume! && count >= 6).

            bean._moduleDescription.append("    when ?Token"
                    + signalName.trim() + "Consume (t>=0) may  goto "
                    + actor.getName().trim() + "_Port_" + signalName.trim()
                    + "_TokenEmpty" + "; \n");

            bean._moduleDescription.append("} \n");
        }

        // Then we generate description regarding states.
        // Note that all guard signal should be renamed as ND_signalName
        // ND stands for non-delayed signals.

        // Enumerate all states in the FSMActor
        HashSet<State> frontier = null;
        frontier = _enumerateStateSet(actor);

        // Decide variables encoded in the Kripke Structure.
        // Note that here the variable only contains inner variables.
        HashSet<String> variableSet = null;
        HashMap<String, String> initialValueSet = null;

        // Enumerate all variables used in the Kripke structure
        variableSet = _decideVariableSet(actor, span);
        initialValueSet = _retrieveVariableInitialValue(actor, variableSet);

        if (variableSet != null) {
            // Add up variable into the variable.
            Iterator<String> variables = variableSet.iterator();
            while (variables.hasNext()) {
                String variableName = variables.next();
                VariableInfo individual = _variableInfo.get(variableName);
                if (individual != null) {
                    if (individual._maxValue != null
                            && individual._minValue != null) {
                        bean._declaredVariables.append("global discrete "
                                + actor.getName().trim() + "_" + variableName
                                + ":" + individual._minValue + ".."
                                + individual._maxValue + "; \n");
                        bean._variableInitialDescriptionSet.add(actor.getName()
                                .trim()
                                + "_"
                                + variableName
                                + " == "
                                + initialValueSet.get(variableName) + " ");
                    }
                }
            }
        }

        // Print out all these states
        // Note that with model conversion, we need to create an additional initial state
        // which changes every constraint of (t>0) in the edge to (t>=0). This is because
        // for the initial state, it is OK to fire at time equals to 0.

        Iterator<State> ite = frontier.iterator();
        while (ite.hasNext()) {
            State state = ite.next();
            if (actor.getInitialState() == state) {
                bean._moduleDescription.append("mode " + actor.getName().trim()
                        + "_State_" + state.getName().trim() + "_Plum"
                        + " ( true ) { \n");
                ArrayList<REDTransitionBean> transitionListWithinState = _generateTransition(
                        actor, state, variableSet, globalSynchronizerSet);
                // Append each of the transition into the module description.
                for (REDTransitionBean transition : transitionListWithinState) {
                    // Evaluate whether the edge is a complementary edge. If so, its constraints should be
                    // further restricted by those "non-complementary edges".
                    if (transition._isComplementaryEdge == false) {
                        if (transition._postCondition.toString().trim()
                                .equalsIgnoreCase("")) {
                            if (transition._preCondition.toString().trim()
                                    .equalsIgnoreCase("true")) {
                                /* When the precondition is true, then any arrival of tokens can trigger the transition. */
                                Iterator<IOPort> it2 = actor.inputPortList()
                                        .iterator();
                                while (it2.hasNext()) {
                                    String signalName = it2.next().getName();

                                    bean._moduleDescription.append("    when "
                                            + transition._signal.toString()
                                            + "?ND_"
                                            + signalName.trim()
                                            + " (t>=0) may "
                                            + transition._postCondition
                                            .toString() + " t=0; goto "
                                            + transition._newState.toString()
                                            + " ;\n");
                                }
                            } else if (transition._preCondition.toString()
                                    .trim().equalsIgnoreCase("")) {

                                bean._moduleDescription.append("    when "
                                        + transition._signal.toString()
                                        + " (t>=0) may "
                                        + transition._postCondition.toString()
                                        + " t=0; goto "
                                        + transition._newState.toString()
                                        + " ;\n");

                            } else {

                                bean._moduleDescription.append("    when "
                                        + transition._signal.toString() + " ("
                                        + transition._preCondition.toString()
                                        + " && t>=0) may "
                                        + transition._postCondition.toString()
                                        + " t=0; goto "
                                        + transition._newState.toString()
                                        + " ;\n");

                            }
                        } else if (transition._postCondition.toString().trim()
                                .endsWith(";")) {
                            if (transition._preCondition.toString().trim()
                                    .equalsIgnoreCase("true")) {
                                /* When the precondition is true, then any arrival of tokens can trigger the transition. */
                                Iterator<IOPort> it2 = actor.inputPortList()
                                        .iterator();
                                while (it2.hasNext()) {
                                    String signalName = it2.next().getName();

                                    bean._moduleDescription.append("    when "
                                            + transition._signal.toString()
                                            + "?"
                                            + signalName.trim()
                                            + " (t>=0) may "
                                            + transition._postCondition
                                            .toString() + " t=0; goto "
                                            + transition._newState.toString()
                                            + " ;\n");
                                }
                            } else if (transition._preCondition.toString()
                                    .trim().equalsIgnoreCase("")) {

                                bean._moduleDescription.append("    when "
                                        + transition._signal.toString()
                                        + " (t>=0) may "
                                        + transition._postCondition.toString()
                                        + " t=0; goto "
                                        + transition._newState.toString()
                                        + " ;\n");

                            } else {
                                bean._moduleDescription.append("    when "
                                        + transition._signal.toString() + " ("
                                        + transition._preCondition.toString()
                                        + "&& t>=0 ) may "
                                        + transition._postCondition.toString()
                                        + " t=0; goto "
                                        + transition._newState.toString()
                                        + " ;\n");

                            }
                        } else {
                            if (transition._preCondition.toString().trim()
                                    .equalsIgnoreCase("true")) {
                                // When the precondition is true, then any arrival of tokens can trigger the transition.
                                Iterator<IOPort> it2 = actor.inputPortList()
                                        .iterator();
                                while (it2.hasNext()) {
                                    String signalName = it2.next().getName();
                                    bean._moduleDescription.append("    when "
                                            + transition._signal.toString()
                                            + "?ND_"
                                            + signalName.trim()
                                            + " (t>=0) may "
                                            + transition._postCondition
                                            .toString()
                                            + " ; t=0; goto "
                                            + transition._newState.toString()
                                            + " ;\n");
                                }
                            } else if (transition._preCondition.toString()
                                    .trim().equalsIgnoreCase("")) {
                                bean._moduleDescription.append("    when "
                                        + transition._signal.toString()
                                        + " (t>=0) may "
                                        + transition._postCondition.toString()
                                        + " ; t=0; goto "
                                        + transition._newState.toString()
                                        + " ;\n");

                            } else {
                                bean._moduleDescription.append("    when "
                                        + transition._signal.toString() + " ("
                                        + transition._preCondition.toString()
                                        + " && t>=0) may "
                                        + transition._postCondition.toString()
                                        + " ; t=0; goto "
                                        + transition._newState.toString()
                                        + " ;\n");
                            }
                        }

                    }
                }
                bean._moduleDescription.append("} \n");
            }

            bean._moduleDescription.append("mode " + actor.getName().trim()
                    + "_State_" + state.getName().trim() + " ( true ) { \n");

            ArrayList<REDTransitionBean> transitionListWithinState = _generateTransition(
                    actor, state, variableSet, globalSynchronizerSet);
            // Append each of the transition into the module description.
            for (REDTransitionBean transition : transitionListWithinState) {

                if (transition._postCondition.toString().trim()
                        .equalsIgnoreCase("")) {
                    if (transition._preCondition.toString().trim()
                            .equalsIgnoreCase("true")) {
                        /* When the precondition is true, then any arrival of tokens can trigger the transition. */
                        Iterator<IOPort> it2 = actor.inputPortList().iterator();
                        while (it2.hasNext()) {
                            String signalName = it2.next().getName();

                            bean._moduleDescription.append("    when "
                                    + transition._signal.toString() + "?ND_"
                                    + signalName.trim() + " (t>0) may "
                                    + transition._postCondition.toString()
                                    + "  t = 0; goto "
                                    + transition._newState.toString() + " ;\n");
                        }
                    } else if (transition._preCondition.toString().trim()
                            .equalsIgnoreCase("")) {
                        bean._moduleDescription.append("    when "
                                + transition._signal.toString() + " (t>0) may "
                                + transition._postCondition.toString()
                                + "  t = 0; goto "
                                + transition._newState.toString() + " ;\n");
                    } else {
                        bean._moduleDescription.append("    when "
                                + transition._signal.toString() + " ("
                                + transition._preCondition.toString()
                                + " && t>0) may "
                                + transition._postCondition.toString()
                                + "  t = 0; goto "
                                + transition._newState.toString() + " ;\n");
                    }

                } else if (transition._postCondition.toString().trim()
                        .endsWith(";")) {
                    if (transition._preCondition.toString().trim()
                            .equalsIgnoreCase("true")) {
                        /* When the precondition is true, then any arrival of tokens can trigger the transition. */
                        Iterator<IOPort> it2 = actor.inputPortList().iterator();
                        while (it2.hasNext()) {
                            String signalName = it2.next().getName();
                            bean._moduleDescription.append("    when "
                                    + transition._signal.toString() + "?ND_"
                                    + signalName.trim() + " (t>0) may "
                                    + transition._postCondition.toString()
                                    + " t = 0; goto "
                                    + transition._newState.toString() + " ;\n");
                        }
                    } else if (transition._preCondition.toString().trim()
                            .equalsIgnoreCase("")) {
                        bean._moduleDescription.append("    when "
                                + transition._signal.toString() + " (t>0) may "
                                + transition._postCondition.toString()
                                + " t = 0; goto "
                                + transition._newState.toString() + " ;\n");
                    } else {
                        bean._moduleDescription.append("    when "
                                + transition._signal.toString() + " ("
                                + transition._preCondition.toString()
                                + " && t>0 ) may "
                                + transition._postCondition.toString()
                                + " t = 0; goto "
                                + transition._newState.toString() + " ;\n");
                    }

                } else {
                    if (transition._preCondition.toString().trim()
                            .equalsIgnoreCase("true")) {
                        /* When the precondition is true, then any arrival of tokens can trigger the transition. */
                        Iterator<IOPort> it2 = actor.inputPortList().iterator();
                        while (it2.hasNext()) {
                            String signalName = it2.next().getName();
                            bean._moduleDescription.append("    when "
                                    + transition._signal.toString() + "?ND_"
                                    + signalName.trim() + " (t>0) may "
                                    + transition._postCondition.toString()
                                    + " ; t = 0; goto "
                                    + transition._newState.toString() + " ;\n");
                        }
                    } else if (transition._preCondition.toString().trim()
                            .equalsIgnoreCase("")) {
                        bean._moduleDescription.append("    when "
                                + transition._signal.toString() + " (t>0) may "
                                + transition._postCondition.toString()
                                + " ; t = 0; goto "
                                + transition._newState.toString() + " ;\n");
                    } else {
                        bean._moduleDescription.append("    when "
                                + transition._signal.toString() + " ("
                                + transition._preCondition.toString()
                                + " && t>0) may "
                                + transition._postCondition.toString()
                                + " ; t = 0; goto "
                                + transition._newState.toString() + " ;\n");
                    }
                }
            }
            bean._moduleDescription.append("} \n");
        }
        return bean;

    }

    /**
     * This is an utility function which performs the translation of a single
     * TimedDelay actor into the format of communicating timed automata (CTA)
     * acceptable by model checker RED.
     *
     * @param delayedActor actor which needs to be converted
     * @param inputSignalName The name of the input signal. This must be derived externally.
     * @param outputSignalName
     *                The name of the output signal. This must be derived
     *                externally.
     * @param bufferSize The defined buffer size used in verification
     * @return description of the TimedDelayActor acceptable by model checker
     *         RED.
     * @exception IllegalActionException
     */
    private static REDSingleEntityBean _translateTimedDelayedActor(
            TimedDelay delayedActor, String inputSignalName,
            String outputSignalName, int bufferSize)
                    throws IllegalActionException {

        // If we expect to convert a TimedDelayedActor into a timed
        // automata, we need to have the following information from the
        // BoundedBufferTimedDelay actor:
        // (1) delay time:
        // (2) size of buffer:
        //
        // Also, we need to retrieve the information for the name of
        // input and output signals. These (inputSignalName, outputSignalName)
        // should be analyzed by callers and pass to this function.
        //

        // Retrieve parameters from clockActors
        double delay = ((DoubleToken) delayedActor.delay.getToken())
                .doubleValue();

        REDSingleEntityBean bean = new REDSingleEntityBean();

        bean._defineConstants.append("#define " + delayedActor.getName().trim()
                + "_DELAY " + String.valueOf((int) delay) + "\n");

        REDModuleNameInitialBean innerBean = new REDModuleNameInitialBean();
        innerBean._name = delayedActor.getName().trim();
        // innerBean._initialStateDescription =
        StringBuffer str = new StringBuffer(delayedActor.getName().trim()
                + "_S");
        for (int i = 0; i < bufferSize; i++) {
            str.append("0");
        }
        innerBean._initialStateDescription = str.toString();
        bean._nameInitialState = innerBean;

        // Based on different buffer size, we need to generate the delayedActor
        // which represents the buffer situation.
        // For a delayedActor with buffer size 1, the we need two states:
        // delayedActor_0 to represent no token.
        // delayedActor_1 to represent one token.
        //
        // Also, clocks used by the system is related to the size of the buffer.
        // We need to capture this behavior.
        //
        // Decide number of clocks used in the system.
        for (int i = 0; i < bufferSize; i++) {
            bean._clockSet.add(delayedActor.getName().trim() + "_C"
                    + String.valueOf(i));

        }

        // Generate strings with arbitrary combination of 0 and 1s of fixed
        // length bufferSize. For the case of buffer size is 3,
        // then we generate 000, 001, 010, 011, 100, 101, 110, 111.
        // These separate numbers would represent different states
        // indicating the status of buffers.

        ArrayList<String> initial = new ArrayList<String>();
        initial.add("");
        ArrayList<String> stringList = _enumerateString(bufferSize, initial);

        // Generate the state based on the content in the stringList
        bean._moduleDescription.append("\n/* Process name: "
                + delayedActor.getName().trim() + " */\n");
        for (String content : stringList) {

            bean._moduleDescription.append("mode "
                    + delayedActor.getName().trim() + "_S" + content.trim()
                    + " (");
            char[] charContent = content.toCharArray();

            StringBuffer StateClockConstraint = new StringBuffer("");
            StringBuffer StateTransitionCondition = new StringBuffer("");
            boolean clockAssigned = false;

            if (charContent.length != 0) {
                for (int i = charContent.length - 1; i >= 0; i--) {
                    if (charContent[i] == '0') {

                        // We don't need to add clock constraints.

                        // when ?inputSignal(TimedDelay2_Ci <=
                        // TIMEDDELAY2_DELAY) may TimedDelay2_Ci = 0; goto
                        // TimedDelay2_SXX1;
                        //
                        // Note that we are not setting all clocks.
                        // Instead, we would set up only one clock.
                        // For example, in 1(C0)0(C1)0(C2), we would only set up
                        // C2.
                        // In 0(C0)0(C1)1(C2), we would only set up C1, but not
                        // C0.
                        if (clockAssigned == false) {
                            char[] newStateContent = content.toCharArray();

                            newStateContent[i] = '1';
                            StateTransitionCondition.append("    when ?"
                                    + inputSignalName.trim() + " (true) may "
                                    + delayedActor.getName().trim() + "_C"
                                    + String.valueOf(i) + " = 0 ; goto "
                                    + delayedActor.getName().trim() + "_S"
                                    + String.valueOf(newStateContent) + "; \n");
                            clockAssigned = true;
                        }

                    } else {
                        // (charContent[i] == '1')
                        // We need to add up clock constraints
                        if (StateClockConstraint.toString()
                                .equalsIgnoreCase("")) {
                            StateClockConstraint
                            .append(" " + delayedActor.getName().trim()
                                    + "_C" + String.valueOf(i) + "<= "
                                    + delayedActor.getName().trim()
                                    + "_DELAY ");
                        } else {
                            StateClockConstraint
                            .append(" && "
                                    + delayedActor.getName().trim()
                                    + "_C" + String.valueOf(i) + "<= "
                                    + delayedActor.getName().trim()
                                    + "_DELAY ");
                        }

                        // when !D_Pgo (TimedDelay2_C1 == TIMEDDELAY2_DELAY) may
                        // goto TimedDelay2_S00;
                        char[] newStateContent = content.toCharArray();
                        newStateContent[i] = '0';
                        StateTransitionCondition.append("    when " + "!"
                                + outputSignalName.trim() + " ("
                                + delayedActor.getName().trim() + "_C"
                                + String.valueOf(i) + " == "
                                + delayedActor.getName().trim()
                                + "_DELAY ) may goto "
                                + delayedActor.getName().trim() + "_S"
                                + String.valueOf(newStateContent) + "; \n");
                        if (i == 0) {
                            if (content.contains("0") == false) {
                                // All true cases. then we need to represent one
                                // case
                                // for overflow.
                                StateTransitionCondition
                                .append("    when ?"
                                        + outputSignalName.trim()
                                        + " (true) may goto Buffer_Overflow; \n");
                            }
                        }

                    }
                }
                if (StateClockConstraint.toString().trim().equalsIgnoreCase("")) {
                    bean._moduleDescription.append("true ) { \n");
                } else {
                    bean._moduleDescription.append(StateClockConstraint
                            .toString() + " ) { \n");
                }

                bean._moduleDescription.append(StateTransitionCondition);
                bean._moduleDescription.append("}\n");

            }

        }

        return bean;
    }

    private static HashMap<String, VariableInfo> _variableInfo;

    ///////////////////////////////////////////////////////////////////
    ////                         inner class                       ////
    private static class VariableInfo {
        private VariableInfo(String paraMax, String paraMin) {

            _maxValue = paraMax;
            _minValue = paraMin;
        }

        private String _maxValue;
        private String _minValue;

    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner class                       ////
    private static class REDSingleEntityBean {
        private REDSingleEntityBean() {
            _defineConstants = new StringBuffer("");
            _declaredVariables = new StringBuffer("");
            _moduleDescription = new StringBuffer("");
            _clockSet = new HashSet<String>();
            _variableInitialDescriptionSet = new HashSet<String>();
            _portSet = new HashSet<REDModuleNameInitialBean>();
            _nameInitialState = new REDModuleNameInitialBean();
        }

        private StringBuffer _defineConstants;
        private StringBuffer _declaredVariables;
        private StringBuffer _moduleDescription;
        private HashSet<String> _clockSet;
        private HashSet<String> _variableInitialDescriptionSet;
        private HashSet<REDModuleNameInitialBean> _portSet;
        private REDModuleNameInitialBean _nameInitialState;

    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner class                       ////
    private static class REDModuleNameInitialBean {
        private REDModuleNameInitialBean() {

        }

        private String _name = "";
        private String _initialStateDescription = "";
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner class                       ////
    private static class REDTransitionBean {
        private REDTransitionBean() {

        }

        private StringBuffer _signal = new StringBuffer("");
        private StringBuffer _preCondition = new StringBuffer("");
        private StringBuffer _postCondition = new StringBuffer("");
        private StringBuffer _newState = new StringBuffer("");
        // private boolean _hasSignal = false;
        private boolean _isComplementaryEdge = false;
        private HashSet<String> _signalSet = new HashSet<String>();
        private ArrayList<String> _complementedCondition = new ArrayList<String>();

    }
}
