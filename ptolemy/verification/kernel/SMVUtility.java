/* An utility function for traversing the system and generate files for model checking using NuSMV.

 Copyright (c) 2008-2014 The Regents of the University of California.
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
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.TypedActor;
import ptolemy.data.BooleanToken;
import ptolemy.domains.modal.kernel.FSMActor;
import ptolemy.domains.modal.kernel.State;
import ptolemy.domains.modal.kernel.Transition;
import ptolemy.domains.modal.kernel.fmv.FmvAutomaton;
import ptolemy.domains.modal.kernel.fmv.FmvState;
import ptolemy.domains.modal.modal.ModalModel;
import ptolemy.domains.sr.kernel.SRDirector;
import ptolemy.kernel.ComponentPort;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.verification.lib.SMVLegacyCodeActor;

///////////////////////////////////////////////////////////////////
// //SMVUtility

/**
 * This is an utility function for Ptolemy II models. It performs a systematic
 * traversal of the system and generate NuSMV (or Cadence SMV) acceptable files
 * for model checking.
 * <p>
 * FIXME: A new version for users to specify the integer bound without using
 * abstraction of "LS" and "GT" should be implemented to support complicated
 * update functions. Note that this has already been implemented in REDUtility.java
 * since the format of RED 7.0 does not support these features.
 *
 * @deprecated ptolemy.de.lib.TimedDelay is deprecated, use ptolemy.actor.lib.TimeDelay.
 * @author Chih-Hong Cheng, Contributor: Edward A. Lee, Christopher
 *         Brooks
 * @version $Id$
 * @since Ptolemy II 8.0
 * @Pt.ProposedRating Red (patrickj)
 * @Pt.AcceptedRating Red (patrickj)
 */
@Deprecated
public class SMVUtility {

    /**
     * This function generates the reachability/risk specification of a
     * system by scanning through the subsystem, and extract states which have
     * special risk or reachability labels.
     *
     * @param model The system model under analysis.
     * @param specType The type of the graphical specification, it may be either
     *                "Risk" or "Reachability"
     * @return A string indicating the CTL formula for risk/reachability
     *         analysis.
     * @exception IllegalActionException
     */
    public static String generateGraphicalSpecification(CompositeActor model,
            String specType) throws IllegalActionException {

        StringBuffer returnSpecStringBuffer = new StringBuffer("");
        HashSet<String> specificationStateSet = _generateGraphicalSpecificationRecursiveStep(
                model, specType, new StringBuffer(""));
        // Combine all these states with conjunctions.
        Iterator<String> stateSetSpecs = specificationStateSet.iterator();
        while (stateSetSpecs.hasNext()) {
            String stateSpec = stateSetSpecs.next();
            if (stateSetSpecs.hasNext()) {
                returnSpecStringBuffer.append(stateSpec + " & ");
            } else {
                returnSpecStringBuffer.append(stateSpec);
            }
        }
        if (specType.equalsIgnoreCase("Risk")) {
            return "!EF(" + returnSpecStringBuffer.toString() + ")";
        } else {
            return " EF(" + returnSpecStringBuffer.toString() + ")";
        }
    }

    /**
     * Return a StringBuffer that contains the converted .smv format of the
     * system. Current algorithm uses a modular approach for construction, enabling us to deal with
     * hierarchical systems. Also recognition of Boolean tokens is supported.
     *
     * <p>For previous implementation, no matter what token is sent through the
     * channel, the receiver only senses the existence of the token by the
     * guard expression <code>XX_isPresent</code>. We now support Boolean token recognition.
     * <p>
     * In order to introduce this mechanism,
     * for each signal XX, two boolean variables are introduced:
     * <br>1) <code>XX_isPresent</code>: indicating whether the signal is present or not.
     * <br>2) <code>XX_value</code>: indicating the value of the signal.
     *
     * <p>Therefore, now in the guard expression, it may be possible to have
     * <br>1) <code>XX_isPresent</code> (in Ptolemy) ==>  <code>XX_isPresent</code> (in SMV)
     * <br>2) <code>XX == 0</code> (in Ptolemy) ==>  <code>XX_isPresent && XX_value == 0</code> (in SMV)
     * <br>3) <code>XX == 1</code> (in Ptolemy) ==>  <code>XX_isPresent && XX_value == 1</code> (in SMV)
     *
     * <p> If XX_isPresent is false, then the value of XX_value is not valid.
     *
     * <p> In SMV, there is no distinguishing between boolean T, F and numerical
     * values 1, 0. So for the sender side, we only need to check if a sender
     * sends a token whose value is not 0 or 1.
     *
     * @param model The system under analysis.
     * @param pattern The temporal formula used to be attached in the .smv file.
     * @param choice The type of the formula. It may be either a CTL or LTL
     *               formula.
     * @param span A constant used to expand the domain of the variable.
     * @return The converted .smv format of the system.
     * @exception IllegalActionException
     * @exception NameDuplicationException
     */
    public static StringBuffer generateSMVDescription(CompositeActor model,
            String pattern, String choice, String span)
                    throws IllegalActionException, NameDuplicationException {

        // Initialization of some global variable storages.
        // See definition for description of these variables.
        _globalSignalDistributionInfo = new HashMap<String, ArrayList<String>>();
        _globalSignalRetrievalInfo = new HashMap<String, HashSet<String>>();
        _globalSignalNestedRetrievalInfo = new HashMap<String, HashSet<String>>();
        _variableInfo = new HashMap<String, VariableInfo>();

        StringBuffer returnSMVFormat = new StringBuffer("");

        // Perform a scan of the whole system in order to retrieve signals
        // used in the system and their locations/visibilities for later
        // analysis.
        _prescanSystemSignal(model, span);

        // List out all FSMs/ModalModels(with refinements) with their
        // states and transitions.

        for (Iterator actors = model.entityList().iterator(); actors.hasNext();) {
            Entity innerEntity = (Entity) actors.next();
            if (innerEntity instanceof FSMActor) {
                // Directly generate the whole description of the system.
                returnSMVFormat.append(_translateSingleFSMActor(
                        (FSMActor) innerEntity, span, false, ""));
            } else if (innerEntity instanceof ModalModel) {
                // We need to generate the description of the subsystem,
                // and also implement the finite state controller.
                ArrayList<StringBuffer> subSystemDescription = _generateSMVDescriptionModalModelWithRefinement(
                        (ModalModel) innerEntity, span, "");
                for (int i = 0; i < subSystemDescription.size(); i++) {
                    returnSMVFormat.append(subSystemDescription.get(i));
                }
            } else if (innerEntity instanceof SMVLegacyCodeActor) {
                // First generate the module description and input parameter.
                returnSMVFormat.append("\nMODULE " + innerEntity.getName()
                        + " (");
                Iterator<Port> itInPortList = ((SMVLegacyCodeActor) innerEntity)
                        .inputPortList().iterator();
                while (itInPortList.hasNext()) {
                    String portName = itInPortList.next().getName();
                    returnSMVFormat.append(portName.trim() + "_isPresent, "
                            + portName.trim() + "_value ");
                    if (itInPortList.hasNext() == true) {
                        returnSMVFormat.append(",");
                    }
                }
                returnSMVFormat.append(")\n");
                // Secondly append user designed module application logic
                // within.
                returnSMVFormat
                .append(((SMVLegacyCodeActor) innerEntity).embeddedSMVCode
                        .getExpression());
            } else if (innerEntity instanceof CompositeActor) {
                // FIXME: Need to add functionalities for dealing with
                // CompositeActors.
                //
                // For composite actors, currently we assume the subsystem
                // should be an SR system. If not, an exception will be thrown
                // out for indication.
                //
                // Potential challenges lie in the visibility of signals.
            }
        }

        StringBuffer mainModuleDescription = new StringBuffer("");
        mainModuleDescription.append("\n\nMODULE main \n");
        mainModuleDescription.append("\tVAR \n");

        for (Iterator actors = model.entityList().iterator(); actors.hasNext();) {
            Entity innerEntity = (Entity) actors.next();
            if (innerEntity instanceof FSMActor
                    || innerEntity instanceof ModalModel) {
                mainModuleDescription.append("\t\t" + innerEntity.getName()
                        + ": " + innerEntity.getName() + "(");

                // Check if the variable has variable outside (invisible
                // in the whole system); where is its location.
                ArrayList<String> signalInfo = _globalSignalDistributionInfo
                        .get(innerEntity.getName());
                if (signalInfo != null) {
                    for (int i = 0; i < signalInfo.size(); i++) {
                        String signalName = signalInfo.get(i);
                        boolean contain = false;
                        String location = "";
                        Iterator<String> it = _globalSignalRetrievalInfo
                                .keySet().iterator();
                        while (it.hasNext()) {
                            String place = it.next();
                            if (_globalSignalRetrievalInfo.get(place) != null) {
                                if (_globalSignalRetrievalInfo.get(place)
                                        .contains(signalName)) {
                                    location = place;
                                    contain = true;
                                    break;
                                }
                            }
                        }
                        if (contain == true) {
                            if (i == signalInfo.size() - 1) {
                                mainModuleDescription.append(location.trim()
                                        + "." + signalName + " ");
                            } else {
                                mainModuleDescription.append(location.trim()
                                        + "." + signalName + ", ");
                            }
                        } else {
                            // Outside scope; use 1 to represent the signal
                            if (i == signalInfo.size() - 1) {
                                // nusmv 2.5.4 requires "TRUE" instead of "1" here.
                                mainModuleDescription.append(" TRUE");
                            } else {
                                // nusmv 2.5.4 requires "TRUE" instead of "1" here.
                                mainModuleDescription.append(" TRUE,");
                            }
                        }
                    }
                }
                mainModuleDescription.append(");\n");

            } else if (innerEntity instanceof SMVLegacyCodeActor) {
                mainModuleDescription.append("\t\t" + innerEntity.getName()
                        + ": " + innerEntity.getName() + "(");
                // Check if the variable has variable outside (invisible
                // in the whole system); where is the location.

                Iterator<Port> inputPortInfo = ((SMVLegacyCodeActor) innerEntity)
                        .inputPortList().iterator();
                while (inputPortInfo.hasNext()) {
                    String signal = inputPortInfo.next().getName().trim();
                    String signalPresent = signal + "_isPresent";
                    boolean containPresent = false;
                    String locationPresent = "";
                    Iterator<String> itPresent = _globalSignalRetrievalInfo
                            .keySet().iterator();
                    while (itPresent.hasNext()) {
                        String place = itPresent.next();
                        if (_globalSignalRetrievalInfo.get(place) != null) {
                            if (_globalSignalRetrievalInfo.get(place).contains(
                                    signalPresent)) {
                                locationPresent = place;
                                containPresent = true;
                                break;
                            }
                        }
                    }
                    if (containPresent == true) {
                        mainModuleDescription.append(locationPresent.trim()
                                + "." + signalPresent + ", ");
                    } else {
                        // use 1 to represent the signal
                        mainModuleDescription.append(" 1,");
                    }

                    String signalValue = signal + "_value";
                    boolean containValue = false;
                    String locationValue = "";
                    Iterator<String> itValue = _globalSignalRetrievalInfo
                            .keySet().iterator();
                    while (itValue.hasNext()) {
                        String place = itValue.next();
                        if (_globalSignalRetrievalInfo.get(place) != null) {
                            if (_globalSignalRetrievalInfo.get(place).contains(
                                    signalValue)) {
                                locationValue = place;
                                containValue = true;
                                break;
                            }
                        }
                    }
                    if (containValue == true) {
                        if (inputPortInfo.hasNext() == false) {
                            mainModuleDescription.append(locationValue.trim()
                                    + "." + signalValue + " ");
                        } else {
                            mainModuleDescription.append(locationValue.trim()
                                    + "." + signalValue + ", ");
                        }
                    } else {
                        // use 1 to represent the signal
                        if (inputPortInfo.hasNext() == false) {
                            mainModuleDescription.append(" 1");
                        } else {
                            mainModuleDescription.append(" 1,");
                        }
                    }

                }
                mainModuleDescription.append(");\n");
            }
        }

        // Lastly, attach the specification into the file.
        if (choice.equalsIgnoreCase("CTL")) {
            mainModuleDescription.append("\n\tSPEC \n");
            mainModuleDescription.append("\t\t" + pattern + "\n");
        } else if (choice.equalsIgnoreCase("LTL")) {
            mainModuleDescription.append("\n\tLTLSPEC \n");
            mainModuleDescription.append("\t\t" + pattern + "\n");
        }

        returnSMVFormat.append(mainModuleDescription);
        return returnSMVFormat;
    }

    /**
     * This function decides if the director of the current actor is SR. If not,
     * return false. This is because our current analysis is only valid when the
     * director is SR.
     *
     * @param model Model used for testing.
     * @return a boolean value indicating if the director is SR.
     */
    public static boolean isValidModelForVerification(CompositeActor model) {
        Director director = model.getDirector();
        if (!(director instanceof SRDirector)) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * This private function first decides signals used in the guard expression
     * of an actor. Those signals should be of the format XX_isPresent. If it is
     * of the format XX == const, we need to check if XX is in the initial
     * parameter list. If yes, then it is a state variable; if not, then it is a
     * signal variable, then use "X_isPresent" or "X_value" to represent it.
     *
     * @param actor The actor under analysis.
     * @return The set of signals used in the guard expression of the actor.
     * @exception IllegalActionException
     */
    private static HashSet<String> _decideGuardSignalVariableSet(FSMActor actor)
            throws IllegalActionException {

        // MODIFICATION 2008.07.21
        // Since this private function first decides signals used in the guard
        // expression of an actor. Those signals should be more than of the
        // format XX_isPresent. It can also have the format like XX == 0 or
        // XX == 1.
        //
        // However, we need to have a mechanism to distinguish between the inner
        // variable and the signal variable, since these are all having the
        // format XX == const.
        //
        // The proposed method is to check from the parameter list.
        // If variable XX is not in the parameter list, then it can be of two
        // choices: 1) XX is the signal variable 2) XX is the state variable,
        // but the user just forgets to place the state variable XX with initial
        // parameter. In this way, later the program would indicate with
        // exceptions.

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
                                // Retrieve the left value of the inequality.
                                String[] characterOfSubGuard = subGuardCondition
                                        .split("(>=)|(<=)|(==)|(!=)|[><]");
                                if (characterOfSubGuard.length > 1) {
                                    // make the judgment whether it is a state
                                    // variable or signal variable.

                                    String attribute = characterOfSubGuard[0]
                                            .trim();
                                    if (actor.getAttribute(attribute) == null) {
                                        // It should "look like" a signal
                                        // variable.
                                        returnVariableSet
                                        .add(characterOfSubGuard[0]
                                                .trim() + "_isPresent");
                                        returnVariableSet
                                        .add(characterOfSubGuard[0]
                                                .trim() + "_value");
                                    }

                                } else {
                                    boolean b = Pattern.matches(".*_isPresent",
                                            characterOfSubGuard[0].trim());
                                    if (b == true) {
                                        if (returnVariableSet
                                                .contains(characterOfSubGuard[0]
                                                        .trim()) == false) {
                                            returnVariableSet
                                            .add(characterOfSubGuard[0]
                                                    .trim());
                                        }
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
     * This private function first decides signal variables that is emitted from
     * the actor. Note that signal variables only appears in outputActions. In
     * order to keep the signal "X" compatible with the appearance in the guard
     * expression "X_isPresent" shown in other actors, we need to attach
     * "_isPresent" with the signal.
     *
     * <p>MODIFICATION 2008.07.21: We need to have "X_isPresent" and "X_value"
     * to represent a variable for new features (supporting boolean tokens).
     *
     * @param actor The actor under analysis
     * @return A set containing names of the signal
     * @exception IllegalActionException
     */
    private static HashSet<String> _decideSignalVariableSet(FSMActor actor)
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

                //boolean hasAnnotation = false;
                String text;
                try {
                    text = transition.annotation.stringValue();
                } catch (IllegalActionException e) {
                    text = "Exception evaluating annotation: " + e.getMessage();
                }
                if (!text.trim().equals("")) {
                    // hasAnnotation = true;
                    // buffer.append(text);
                }

                String expression = transition.outputActions.getExpression();
                if (expression != null && !expression.trim().equals("")) {
                    // Retrieve possible value of the variable
                    String[] splitExpression = expression.split(";");
                    for (String element : splitExpression) {
                        String[] characters = element.split("=");
                        String lValue_isPresent = characters[0].trim()
                                + "_isPresent";

                        // add it into the _variableInfo
                        // see if it exists
                        if (_variableInfo.get(lValue_isPresent) == null) {
                            // Create a new one and insert all info.
                            VariableInfo newVariable = new VariableInfo("1",
                                    "0");
                            _variableInfo.put(lValue_isPresent, newVariable);
                            if (returnVariableSet.contains(lValue_isPresent) == false) {
                                returnVariableSet.add(lValue_isPresent);
                            }
                        } else {
                            if (returnVariableSet.contains(lValue_isPresent) == false) {
                                returnVariableSet.add(lValue_isPresent);
                            }
                        }

                        // MODIFICATION 2008.07.21 Add up "lValue_value" for the
                        // variable for lvalue's value.
                        String lValue_value = characters[0].trim() + "_value";
                        // add it into the _variableInfo
                        // see if it exists
                        if (_variableInfo.get(lValue_value) == null) {
                            // Create a new one and insert all info.
                            VariableInfo newVariable = new VariableInfo("1",
                                    "0");
                            _variableInfo.put(lValue_value, newVariable);
                            if (returnVariableSet.contains(lValue_value) == false) {
                                returnVariableSet.add(lValue_value);
                            }
                        } else {
                            if (returnVariableSet.contains(lValue_value) == false) {
                                returnVariableSet.add(lValue_value);
                            }
                        }
                    }
                }

            }

        }

        return returnVariableSet;
    }

    /**
     * This private function first decides state variables (or called inner variables)
     * that would be used in the Kripke structure. It would first perform a system
     * prescan to have a rough domain for each variable. Then it expands the
     * domain by using the constant span.
     *
     * @param actor The actor under analysis
     * @param numSpan The size to expand the original domain
     * @return The state variable set.
     * @exception IllegalActionException
     */
    private static HashSet<String> _decideStateVariableSet(FSMActor actor,
            int numSpan) throws IllegalActionException {

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
                    // buffer.append(text);
                }

                // Retrieve the guardExpression for checking the existence
                // of inner variables used in FmcAutomaton.
                String guard = transition.getGuardExpression();
                if (guard != null && !guard.trim().equals("")) {
                    if (hasAnnotation) {
                        // do nothing
                    } else {

                        // Rule II. For all variables that are used as
                        // guards, they would be expanded as AP

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
                                    // Currently do nothing.
                                } else {
                                    // MODIFICATION 2008.07.21
                                    // Now we have to detect whether this is the
                                    // state variable or the signal variable.
                                    // Here we also apply the methodology of
                                    // checking the initial parameter for the
                                    // decision.

                                    // make the judgment whether it is a state
                                    // variable or signal variable.

                                    String attribute = characterOfSubGuard[0]
                                            .trim();
                                    if (actor.getAttribute(attribute) != null) {
                                        // It "looks like" a state (inner)
                                        // variable .
                                        //
                                        // Second case, place this variable into
                                        // usage set. Retrieve the rvalue
                                        //
                                        // Check if the right value exists. We
                                        // need to ward off cases like "true".

                                        String rValue = null;
                                        boolean isTrue = false;
                                        try {
                                            // FIXME: FindBugs RV: Base use of return value from method,
                                            // Method ignores return value.  What does this code do?
                                            rValue = characterOfSubGuard[1]
                                                    .trim();
                                        } catch (Exception ex) {
                                            isTrue = true;
                                        }
                                        if (isTrue == false) {
                                            if (Pattern.matches("^-?\\d+$",
                                                    rValue) == true) {
                                                int numberRetrieval = Integer
                                                        .parseInt(rValue);
                                                // add it into the _variableInfo
                                                returnVariableSet
                                                .add(characterOfSubGuard[0]
                                                        .trim());

                                                VariableInfo variable = _variableInfo
                                                        .get(characterOfSubGuard[0]
                                                                .trim());
                                                if (variable != null) {
                                                    if (variable._maxValue != null
                                                            && variable._minValue != null) {
                                                        // modify the existing
                                                        // one
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
                                                        _variableInfo
                                                        .put(characterOfSubGuard[0]
                                                                .trim(),
                                                                variable);
                                                    }
                                                } else {
                                                    // Create a new one and
                                                    // insert all info.
                                                    VariableInfo newVariable = new VariableInfo(

                                                            Integer.toString(numberRetrieval),
                                                            Integer.toString(numberRetrieval));
                                                    _variableInfo
                                                    .put(characterOfSubGuard[0]
                                                            .trim(),
                                                            newVariable);

                                                }
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
            if (individual != null) {
                try {
                    int lbOriginal = Integer.parseInt(individual._minValue);
                    int ubOriginal = Integer.parseInt(individual._maxValue);
                    int lbNew = lbOriginal - (ubOriginal - lbOriginal + 1)
                            * numSpan;
                    int ubNew = ubOriginal + (ubOriginal - lbOriginal + 1)
                            * numSpan;
                    individual._minValue = Integer.toString(lbNew);
                    individual._maxValue = Integer.toString(ubNew);
                    _variableInfo.put(valName, individual);

                } catch (Exception ex) {
                    throw new IllegalActionException(
                            "SMVUtility._decideVariableSet() clashes: "
                                    + ex.getMessage());
                }
            }
        }

        return returnVariableSet;
    }

    /**
     * Perform an enumeration of the state in this FmvAutomaton and return a
     * HashSet of states.
     *
     * @param actor The actor under analysis
     * @return A HashSet of states of a particular FSMActor
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
            String name = stateInThis == null ? "" : stateInThis.getName();
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
                if (stateInThis != null) {
                    ComponentPort outPort = stateInThis.outgoingPort;
                    Iterator transitions = outPort.linkedRelationList()
                            .iterator();

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
            }
        } catch (Exception exception) {
            throw new IllegalActionException(
                    "SMVUtility._enumerateStateSet() clashes: "
                            + exception.getMessage());

        }
        return returnStateSet;
    }

    /**
     * Generate all premise-action pairs regarding this automaton. For
     * example, this method may generate (state=red)&&(count=1):{grn}. The
     * premise is "(state=red)&&(count=1)", and the action is "{grn}" This can
     * only be applied when the domain of variable is decided.
     *
     * @param actor The actor under analysis
     * @param variableSet The set of variables used
     * @exception IllegalActionException
     */
    private static void _generateAllVariableTransitions(FSMActor actor,
            HashSet<String> variableSet) throws IllegalActionException {

        HashSet<State> stateSet = new HashSet<State>();

        // Initialize
        HashMap<String, State> frontier = new HashMap<String, State>();

        _variableTransitionInfo = new HashMap<String, LinkedList<VariableTransitionInfo>>();
        Iterator<String> vit = variableSet.iterator();
        while (vit.hasNext()) {
            String v = vit.next();
            LinkedList<VariableTransitionInfo> l = new LinkedList<VariableTransitionInfo>();
            _variableTransitionInfo.put(v, l);
        }
        LinkedList<VariableTransitionInfo> l = new LinkedList<VariableTransitionInfo>();
        _variableTransitionInfo.put("state", l);

        // Create initial state
        State stateInThis = actor.getInitialState();
        String name = stateInThis.getName();
        frontier.put(name, stateInThis);

        // Iterate
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
                // signalPremise: used to store premise of signals
                StringBuffer signalPremise = new StringBuffer("");

                Transition transition = (Transition) transitions.next();
                State destinationInThis = transition.destinationState();

                if (!stateSet.contains(destinationInThis)) {
                    frontier.put(destinationInThis.getName(), destinationInThis);
                    stateSet.add(destinationInThis);
                }

                // Retrieve the transition

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

                // Retrieve the variable used in the Kripke structure.
                // Also analyze the guard expression to understand the
                // possible value domain for the value to execute.
                //
                // A guard expression would need to be separated into
                // separate sub-statements in order to estimate the boundary
                // of the variable. Note that we need to tackle cases where
                // a>-1 and a<5 happen simultaneously. Also we expect to
                // constrain the way that an end user can do for writing
                // codes. We do "not" expect him to write in the way like
                // -1<a.
                //
                // Also here we assume that every sub-guard expression is
                // connected using && but not || operator. But it is easy to
                // modify the code such that it supports ||.
                //

                String guard = transition.getGuardExpression();
                String setAction = transition.setActions.getExpression();
                String outputAction = transition.outputActions.getExpression();

                // variableUsedInTransitionSet: Store variable names used in
                // this transition as preconditions. If in the guard
                // expression, we have X<3 && Y>5, then X and Y are used as
                // variables in precondition and should be stored in the
                // set "variableUsedInTransitionSet".

                HashSet<String> variableUsedInTransitionSet = new HashSet<String>();

                if (guard != null && !guard.trim().equals("")) {
                    if (hasAnnotation) {

                    } else {

                        // Rule II. For all variables that are used as
                        // guards, they would be expanded as Atomic
                        // Propositions (AP).

                        // Separate each guard expression into "sub guard
                        // expressions".
                        String[] guardSplitExpression = guard.split("(&&)");

                        if (guardSplitExpression.length != 0) {
                            for (String element : guardSplitExpression) {
                                // Trim tab/space
                                String subGuardCondition = element.trim();

                                // Retrieve the left value of the
                                // inequality. Here we may still have two
                                // cases for the lValue:
                                // (1) XXX_isPresent (2) the normal case
                                // (including "true").
                                String[] characterOfSubGuard = subGuardCondition
                                        .split("(>=)|(<=)|(==)|(!=)|[><]");

                                String lValue = characterOfSubGuard[0].trim();
                                boolean b = Pattern.matches(".*_isPresent",
                                        characterOfSubGuard[0].trim());
                                if (b == true) {
                                    // We add it into the list for transition.
                                    signalPremise.append(characterOfSubGuard[0]
                                            .trim() + " & ");

                                } else {
                                    // Store in the set. Use try-catch to;
                                    // capture cases when single "true"
                                    // exists.
                                    boolean isTrue = false;
                                    try {
                                        characterOfSubGuard[1] = characterOfSubGuard[1]
                                                .trim();
                                    } catch (Exception ex) {
                                        isTrue = true;
                                    }
                                    if (isTrue == false) {
                                        // Examine if the variable is a signal
                                        // variable.
                                        String attribute = characterOfSubGuard[0]
                                                .trim();
                                        if (actor.getAttribute(attribute) == null) {
                                            // It "looks like" a signal
                                            // variable. The format is
                                            // XX == 1
                                            // We thus need to append XX_value
                                            // and XX_isPresent
                                            variableUsedInTransitionSet
                                            .add(lValue.trim()
                                                    + "_isPresent");
                                            variableUsedInTransitionSet
                                            .add(lValue.trim()
                                                    + "_value");
                                        } else {
                                            // It "looks like" a state variable
                                            variableUsedInTransitionSet
                                            .add(lValue);
                                        }

                                    }

                                }
                            }
                        }
                    }
                }

                if (setAction != null && !setAction.trim().equals("")) {

                    String[] setActionSplitExpression = setAction.split("(;)");

                    if (setActionSplitExpression.length != 0) {
                        for (String element : setActionSplitExpression) {
                            // Trim tab/space
                            String subSetActionCondition = element.trim();

                            String[] characterOfSubSetAction = subSetActionCondition
                                    .split("(=)");

                            String lValue = characterOfSubSetAction[0].trim();
                            variableUsedInTransitionSet.add(lValue);

                        }
                    }

                }

                if (outputAction != null && !outputAction.trim().equals("")) {
                    String[] outputActionSplitExpression = outputAction
                            .split("(;)");
                    if (outputActionSplitExpression.length != 0) {
                        for (String element : outputActionSplitExpression) {
                            // Trim tab/space
                            String subOutputActionCondition = element.trim();

                            String[] characterOfSubOutputAction = subOutputActionCondition
                                    .split("(=)");

                            String lValue = characterOfSubOutputAction[0]
                                    .trim();

                            variableUsedInTransitionSet.add(lValue
                                    + "_isPresent");
                            variableUsedInTransitionSet.add(lValue + "_value");

                        }
                    }
                }

                // Once all variables used in the transition is listed,
                // generate a list to estimate its domain. For example, if
                // variable X has upper bound 5 and lower bound 1, then the
                // result of the next step would show that variable X has a
                // list with domain {1,2,3,4,5}.

                HashMap<String, ArrayList<Integer>> valueDomain = new HashMap<String, ArrayList<Integer>>();
                Iterator<String> it = variableUsedInTransitionSet.iterator();
                while (it.hasNext()) {
                    String val = it.next();
                    boolean b1 = Pattern.matches(".*_isPresent", val);
                    boolean b2 = Pattern.matches(".*_value", val);
                    if (b1 == true || b2 == true) {
                        // For those variables, they only have true (1)
                        // and false (0) value.

                        VariableInfo variableInfo = _variableInfo.get(val);
                        if (variableInfo == null) {
                            throw new IllegalActionException(
                                    "Internal error, removing \"" + val
                                    + "\" returned null?");
                        } else {
                            if (variableInfo._minValue != null
                                    && variableInfo._maxValue != null) {
                                int lowerBound = Integer
                                        .parseInt(variableInfo._minValue);
                                int upperBound = Integer
                                        .parseInt(variableInfo._maxValue);

                                ArrayList<Integer> variableDomainForTransition = new ArrayList<Integer>();

                                for (int number = lowerBound; number <= upperBound; number++) {
                                    // Place each possible value within boundary
                                    // into the list.
                                    variableDomainForTransition.add(Integer
                                            .valueOf(number));
                                }
                                valueDomain.put(val,
                                        variableDomainForTransition);
                            }
                        }
                    } else {

                        VariableInfo variableInfo = _variableInfo.get(val);
                        if (variableInfo == null) {
                            throw new IllegalActionException(
                                    "Internal error, removing \"" + val
                                    + "\" returned null?");
                        } else {
                            if (variableInfo._minValue != null
                                    && variableInfo._maxValue != null) {
                                int lowerBound = Integer
                                        .parseInt(variableInfo._minValue);
                                int upperBound = Integer
                                        .parseInt(variableInfo._maxValue);
                                // Now perform the add up of new value:
                                // DOMAIN_GT and DOMAIN_LS into each of the
                                // variableDomainForTransition set.
                                // We make it a sorted list to facilitate
                                // further processing.
                                ArrayList<Integer> variableDomainForTransition = new ArrayList<Integer>();
                                variableDomainForTransition.add(DOMAIN_LS);
                                for (int number = lowerBound; number <= upperBound; number++) {
                                    // Place each possible value within boundary
                                    // into the list.
                                    variableDomainForTransition.add(Integer
                                            .valueOf(number));
                                }
                                variableDomainForTransition.add(DOMAIN_GT);

                                valueDomain.put(val,
                                        variableDomainForTransition);
                            }
                        }
                    }

                }

                // After previous steps, for each variable now there
                // exists a list with all possible values between lower
                // bound and upper bound. Now perform the restriction
                // process based on the guard expression. For example, if
                // variable X has upper bound 5 and lower bound 1, and the
                // guard expression says that X<3, then the domain would be
                // restricted to only {1,2}.

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
                                String lValue = characterOfSubGuard[0].trim();
                                boolean b = Pattern.matches(".*_isPresent",
                                        characterOfSubGuard[0].trim());
                                if (b == true) {

                                } else {
                                    // Check if the right value exists. We
                                    // need to ward off cases like "true".
                                    // This is achieved using try-catch and
                                    // retrieve the rValue from
                                    // characterOfSubGuard[1].
                                    boolean parse = true;
                                    String rValue = null;
                                    try {
                                        rValue = characterOfSubGuard[1].trim();
                                    } catch (Exception ex) {
                                        parse = false;
                                    }
                                    if (parse == true) {
                                        // MODIFICATION: Examine first whether
                                        // this is a signal variable. For signal
                                        // variable XX, we need to restrict
                                        // XX_isPresent to 1 and XX_value based
                                        // on the constraint.

                                        boolean isSignalVariable = false;

                                        String attribute = characterOfSubGuard[0]
                                                .trim();
                                        if (actor.getAttribute(attribute) == null) {
                                            isSignalVariable = true;
                                        }
                                        if (Pattern.matches("^-?\\d+$", rValue) == true) {
                                            int numberRetrieval = Integer
                                                    .parseInt(rValue);

                                            // We need to understand what is
                                            // the operator of the value in
                                            // order to reason the bound of
                                            // the variable for suitable
                                            // transition.

                                            if (Pattern.matches(".*==.*",
                                                    subGuardCondition)) {
                                                // equal than, restrict the
                                                // set of all possible
                                                // values in the domain into
                                                // one single value.

                                                if (isSignalVariable == true) {
                                                    ArrayList<Integer> domainIsPresent = valueDomain
                                                            .remove(lValue
                                                                    .trim()
                                                                    + "_isPresent");

                                                    if (domainIsPresent == null) {
                                                        throw new IllegalActionException(
                                                                "Internal error, removing \""
                                                                        + lValue
                                                                        + "\" returned null?");
                                                    } else {
                                                        for (int j = domainIsPresent
                                                                .size() - 1; j >= 0; j--) {
                                                            if (domainIsPresent
                                                                    .get(j)
                                                                    .intValue() != numberRetrieval) {
                                                                domainIsPresent
                                                                .remove(j);
                                                            }
                                                        }
                                                        valueDomain
                                                        .put(lValue
                                                                .trim()
                                                                + "_isPresent",
                                                                domainIsPresent);
                                                    }
                                                    ArrayList<Integer> domainValue = valueDomain
                                                            .remove(lValue
                                                                    .trim()
                                                                    + "_value");

                                                    if (domainValue == null) {
                                                        throw new IllegalActionException(
                                                                "Internal error, removing \""
                                                                        + lValue
                                                                        + "\" returned null?");
                                                    } else {
                                                        for (int j = domainValue
                                                                .size() - 1; j >= 0; j--) {
                                                            if (domainValue
                                                                    .get(j)
                                                                    .intValue() != numberRetrieval) {
                                                                domainValue
                                                                .remove(j);
                                                            }
                                                        }
                                                        valueDomain
                                                        .put(lValue
                                                                .trim()
                                                                + "_value",
                                                                domainValue);
                                                    }
                                                } else {
                                                    ArrayList<Integer> domain = valueDomain
                                                            .remove(lValue);

                                                    if (domain == null) {
                                                        throw new IllegalActionException(
                                                                "Internal error, removing \""
                                                                        + lValue
                                                                        + "\" returned null?");
                                                    } else {
                                                        for (int j = domain
                                                                .size() - 1; j >= 0; j--) {
                                                            if (domain.get(j)
                                                                    .intValue() != numberRetrieval) {
                                                                domain.remove(j);
                                                            }
                                                        }
                                                        valueDomain.put(lValue,
                                                                domain);
                                                    }
                                                }

                                            } else if (Pattern
                                                    .matches(".*!=.*",
                                                            subGuardCondition)) {
                                                // not equal

                                                if (isSignalVariable == true) {
                                                    ArrayList<Integer> domainIsPresent = valueDomain
                                                            .remove(lValue
                                                                    .trim()
                                                                    + "_isPresent");

                                                    if (domainIsPresent == null) {
                                                        throw new IllegalActionException(
                                                                "Internal error, removing \""
                                                                        + lValue
                                                                        + "\" returned null?");
                                                    } else {
                                                        for (int j = domainIsPresent
                                                                .size() - 1; j >= 0; j--) {
                                                            if (domainIsPresent
                                                                    .get(j)
                                                                    .intValue() == numberRetrieval) {
                                                                domainIsPresent
                                                                .remove(j);
                                                            }
                                                        }
                                                        valueDomain
                                                        .put(lValue
                                                                .trim()
                                                                + "_isPresent",
                                                                domainIsPresent);
                                                    }
                                                    ArrayList<Integer> domainValue = valueDomain
                                                            .remove(lValue
                                                                    .trim()
                                                                    + "_value");

                                                    if (domainValue == null) {
                                                        throw new IllegalActionException(
                                                                "Internal error, removing \""
                                                                        + lValue
                                                                        + "\" returned null?");
                                                    } else {
                                                        for (int j = domainValue
                                                                .size() - 1; j >= 0; j--) {
                                                            if (domainValue
                                                                    .get(j)
                                                                    .intValue() == numberRetrieval) {
                                                                domainValue
                                                                .remove(j);
                                                            }
                                                        }
                                                        valueDomain
                                                        .put(lValue
                                                                .trim()
                                                                + "_value",
                                                                domainValue);
                                                    }
                                                } else {
                                                    ArrayList<Integer> domain = valueDomain
                                                            .remove(lValue);

                                                    if (domain == null) {
                                                        throw new IllegalActionException(
                                                                "Internal error, removing \""
                                                                        + lValue
                                                                        + "\" returned null?");
                                                    } else {
                                                        for (int j = domain
                                                                .size() - 1; j >= 0; j--) {
                                                            if (domain.get(j)
                                                                    .intValue() == numberRetrieval) {
                                                                domain.remove(j);
                                                            }
                                                        }
                                                        valueDomain.put(lValue,
                                                                domain);
                                                    }
                                                }

                                            } else if (Pattern
                                                    .matches(".*<=.*",
                                                            subGuardCondition)) {
                                                // less or equal than

                                                if (isSignalVariable == true) {
                                                    ArrayList<Integer> domainIsPresent = valueDomain
                                                            .remove(lValue
                                                                    .trim()
                                                                    + "_isPresent");

                                                    if (domainIsPresent == null) {
                                                        throw new IllegalActionException(
                                                                "Internal error, removing \""
                                                                        + lValue
                                                                        + "\" returned null?");
                                                    } else {
                                                        for (int j = domainIsPresent
                                                                .size() - 1; j >= 0; j--) {
                                                            if (domainIsPresent
                                                                    .get(j)
                                                                    .intValue() > numberRetrieval) {
                                                                domainIsPresent
                                                                .remove(j);
                                                            }
                                                        }
                                                        valueDomain
                                                        .put(lValue
                                                                .trim()
                                                                + "_isPresent",
                                                                domainIsPresent);
                                                    }
                                                    ArrayList<Integer> domainValue = valueDomain
                                                            .remove(lValue
                                                                    .trim()
                                                                    + "_value");

                                                    if (domainValue == null) {
                                                        throw new IllegalActionException(
                                                                "Internal error, removing \""
                                                                        + lValue
                                                                        + "\" returned null?");
                                                    } else {
                                                        for (int j = domainValue
                                                                .size() - 1; j >= 0; j--) {
                                                            if (domainValue
                                                                    .get(j)
                                                                    .intValue() > numberRetrieval) {
                                                                domainValue
                                                                .remove(j);
                                                            }
                                                        }
                                                        valueDomain
                                                        .put(lValue
                                                                .trim()
                                                                + "_value",
                                                                domainValue);
                                                    }
                                                } else {
                                                    ArrayList<Integer> domain = valueDomain
                                                            .remove(lValue);

                                                    if (domain == null) {
                                                        throw new IllegalActionException(
                                                                "Internal error, removing \""
                                                                        + lValue
                                                                        + "\" returned null?");
                                                    } else {
                                                        for (int j = domain
                                                                .size() - 1; j >= 0; j--) {
                                                            if (domain.get(j)
                                                                    .intValue() > numberRetrieval) {
                                                                domain.remove(j);
                                                            }
                                                        }
                                                        valueDomain.put(lValue,
                                                                domain);
                                                    }

                                                }

                                            } else if (Pattern
                                                    .matches(".*>=.*",
                                                            subGuardCondition)) {
                                                // greater or equal than

                                                if (isSignalVariable == true) {
                                                    // Deal cases for
                                                    // XX_isPresent
                                                    // and XX_value

                                                    ArrayList<Integer> domainIsPresent = valueDomain
                                                            .remove(lValue
                                                                    .trim()
                                                                    + "_isPresent");

                                                    if (domainIsPresent == null) {
                                                        throw new IllegalActionException(
                                                                "Internal error, removing \""
                                                                        + lValue
                                                                        + "\" returned null?");
                                                    } else {
                                                        for (int j = domainIsPresent
                                                                .size() - 1; j >= 0; j--) {
                                                            if (domainIsPresent
                                                                    .get(j)
                                                                    .intValue() < numberRetrieval) {
                                                                domainIsPresent
                                                                .remove(j);
                                                            }
                                                        }
                                                        valueDomain
                                                        .put(lValue
                                                                .trim()
                                                                + "_isPresent",
                                                                domainIsPresent);
                                                    }
                                                    ArrayList<Integer> domainValue = valueDomain
                                                            .remove(lValue
                                                                    .trim()
                                                                    + "_value");

                                                    if (domainValue == null) {
                                                        throw new IllegalActionException(
                                                                "Internal error, removing \""
                                                                        + lValue
                                                                        + "\" returned null?");
                                                    } else {
                                                        for (int j = domainValue
                                                                .size() - 1; j >= 0; j--) {
                                                            if (domainValue
                                                                    .get(j)
                                                                    .intValue() < numberRetrieval) {
                                                                domainValue
                                                                .remove(j);
                                                            }
                                                        }
                                                        valueDomain
                                                        .put(lValue
                                                                .trim()
                                                                + "_value",
                                                                domainValue);
                                                    }
                                                } else {
                                                    ArrayList<Integer> domain = valueDomain
                                                            .remove(lValue);

                                                    if (domain == null) {
                                                        throw new IllegalActionException(
                                                                "Internal error, removing \""
                                                                        + lValue
                                                                        + "\" returned null?");
                                                    } else {
                                                        for (int j = domain
                                                                .size() - 1; j >= 0; j--) {
                                                            if (domain.get(j)
                                                                    .intValue() < numberRetrieval) {
                                                                domain.remove(j);
                                                            }
                                                        }
                                                        valueDomain.put(lValue,
                                                                domain);
                                                    }
                                                }

                                            } else if (Pattern.matches(".*>.*",
                                                    subGuardCondition)) {

                                                // greater than
                                                if (isSignalVariable == true) {
                                                    // Deal cases for
                                                    // XX_isPresent
                                                    // and XX_value
                                                    ArrayList<Integer> domainIsPresent = valueDomain
                                                            .remove(lValue
                                                                    .trim()
                                                                    + "_isPresent");

                                                    if (domainIsPresent == null) {
                                                        throw new IllegalActionException(
                                                                "Internal error, removing \""
                                                                        + lValue
                                                                        + "\" returned null?");
                                                    }

                                                    for (int j = domainIsPresent
                                                            .size() - 1; j >= 0; j--) {
                                                        if (domainIsPresent
                                                                .get(j)
                                                                .intValue() <= numberRetrieval) {
                                                            domainIsPresent
                                                            .remove(j);
                                                        }
                                                    }
                                                    valueDomain
                                                    .put(lValue.trim()
                                                            + "_isPresent",
                                                            domainIsPresent);

                                                    ArrayList<Integer> domainValue = valueDomain
                                                            .remove(lValue
                                                                    .trim()
                                                                    + "_value");

                                                    if (domainValue == null) {
                                                        throw new IllegalActionException(
                                                                "Internal error, removing \""
                                                                        + lValue
                                                                        + "\" returned null?");
                                                    }

                                                    for (int j = domainValue
                                                            .size() - 1; j >= 0; j--) {
                                                        if (domainValue.get(j)
                                                                .intValue() <= numberRetrieval) {
                                                            domainValue
                                                            .remove(j);
                                                        }
                                                    }
                                                    valueDomain.put(
                                                            lValue.trim()
                                                            + "_value",
                                                            domainValue);
                                                } else {
                                                    ArrayList<Integer> domain = valueDomain
                                                            .remove(lValue);

                                                    if (domain == null) {
                                                        throw new IllegalActionException(
                                                                "Internal error, removing \""
                                                                        + lValue
                                                                        + "\" returned null?");
                                                    }

                                                    for (int j = domain.size() - 1; j >= 0; j--) {
                                                        if (domain.get(j)
                                                                .intValue() <= numberRetrieval) {
                                                            domain.remove(j);
                                                        }
                                                    }
                                                    valueDomain.put(lValue,
                                                            domain);
                                                }

                                            } else if (Pattern.matches(".*<.*",
                                                    subGuardCondition)) {
                                                // less than
                                                if (isSignalVariable == true) {
                                                    // Deal cases for
                                                    // XX_isPresent
                                                    // and XX_value
                                                    ArrayList<Integer> domainIsPresent = valueDomain
                                                            .remove(lValue
                                                                    .trim()
                                                                    + "_isPresent");

                                                    if (domainIsPresent == null) {
                                                        throw new IllegalActionException(
                                                                "Internal error, removing \""
                                                                        + lValue
                                                                        + "\" returned null?");
                                                    } else {
                                                        for (int j = domainIsPresent
                                                                .size() - 1; j >= 0; j--) {
                                                            if (domainIsPresent
                                                                    .get(j)
                                                                    .intValue() >= numberRetrieval) {
                                                                domainIsPresent
                                                                .remove(j);
                                                            }
                                                        }
                                                        valueDomain
                                                        .put(lValue
                                                                .trim()
                                                                + "_isPresent",
                                                                domainIsPresent);
                                                    }
                                                    ArrayList<Integer> domainValue = valueDomain
                                                            .remove(lValue
                                                                    .trim()
                                                                    + "_value");

                                                    if (domainValue == null) {
                                                        throw new IllegalActionException(
                                                                "Internal error, removing \""
                                                                        + lValue
                                                                        + "\" returned null?");
                                                    } else {
                                                        for (int j = domainValue
                                                                .size() - 1; j >= 0; j--) {
                                                            if (domainValue
                                                                    .get(j)
                                                                    .intValue() >= numberRetrieval) {
                                                                domainValue
                                                                .remove(j);
                                                            }
                                                        }
                                                        valueDomain
                                                        .put(lValue
                                                                .trim()
                                                                + "_value",
                                                                domainValue);
                                                    }
                                                } else {

                                                    ArrayList<Integer> domain = valueDomain
                                                            .remove(lValue);

                                                    if (domain == null) {
                                                        throw new IllegalActionException(
                                                                "Internal error, removing \""
                                                                        + lValue
                                                                        + "\" returned null?");
                                                    } else {
                                                        for (int j = domain
                                                                .size() - 1; j >= 0; j--) {
                                                            if (domain.get(j)
                                                                    .intValue() >= numberRetrieval) {
                                                                domain.remove(j);
                                                            }
                                                        }
                                                        valueDomain.put(lValue,
                                                                domain);
                                                    }
                                                }

                                            }

                                        }
                                    }

                                }

                            }
                        }
                    }

                }

                // setActions stores information about the update of the
                // variable; outputActions stores information about the
                // update of the variable that is going to be transmitted
                // through the output port.

                String setActionExpression = transition.setActions
                        .getExpression();

                if (setActionExpression != null
                        && !setActionExpression.trim().equals("")) {
                    // Retrieve possible value of the variable
                    String[] splitExpression = setActionExpression.split(";");
                    for (String element : splitExpression) {
                        String[] characters = element.split("=");
                        if (characters.length >= 1) {
                            String lValue = characters[0].trim();
                            String rValue = characters[1].trim();
                            if (Pattern.matches("^-?\\d+$",
                                    characters[1].trim()) == true) {

                                // Generate all possible conditions that leads
                                // to this change.

                                String statePrecondition = "state="
                                        + stateInThis.getDisplayName();
                                _generatePremiseAndResultEachTransition(
                                        signalPremise.toString()
                                        + statePrecondition,
                                        valueDomain, lValue, rValue, "N");
                                _generatePremiseAndResultEachTransition(
                                        signalPremise.toString()
                                        + statePrecondition,
                                        valueDomain, "state",
                                        destinationInThis.getDisplayName(), "S");

                            } else {
                                // The right hand side is actually complicated
                                // expression which needs to be carefully
                                // Designed for accepting various expression.
                                // If we expect to do this, it is necessary to
                                // construct a parse tree and
                                // evaluate the value.
                                // Currently let us assume that we are
                                // manipulating simple format
                                // a = a op constInt; or a = constInt;

                                if (Pattern.matches(".*\\*.*", rValue)) {

                                    String[] rValueOperends = rValue
                                            .split("\\*");

                                    String offset = rValueOperends[1].trim();

                                    try {
                                        Integer.parseInt(rValueOperends[1]
                                                .trim());
                                    } catch (Exception ex) {
                                        // check if the value is of format (-a)
                                        if (rValueOperends[1].trim().endsWith(
                                                ")")
                                                && rValueOperends[1].trim()
                                                .startsWith("(")) {
                                            // retrieve the value
                                            offset = rValueOperends[1]
                                                    .trim()
                                                    .substring(
                                                            1,
                                                            rValueOperends[1]
                                                                    .trim()
                                                                    .length() - 1);
                                            try {
                                                Integer.parseInt(offset);
                                            } catch (Exception exInner) {
                                                // Return the format is not
                                                // supported by the system.
                                                throw new IllegalActionException(
                                                        "Format not supported by the analysis:"
                                                                + exInner
                                                                .getMessage());
                                            }

                                        }

                                    }
                                    // set up all possible transitions
                                    // regarding to this assignment.

                                    String statePrecondition = "state="
                                            + stateInThis.getDisplayName();

                                    _generatePremiseAndResultEachTransition(
                                            signalPremise.toString()
                                            + statePrecondition,
                                            valueDomain, lValue, offset, "*");
                                    _generatePremiseAndResultEachTransition(
                                            signalPremise.toString()
                                            + statePrecondition,
                                            valueDomain, "state",
                                            destinationInThis.getDisplayName(),
                                            "S");

                                } else if (Pattern.matches(".*/.*", rValue)) {

                                    String[] rValueOperends = rValue
                                            .split("[/]");

                                    String offset = rValueOperends[1].trim();

                                    try {
                                        Integer.parseInt(rValueOperends[1]
                                                .trim());
                                    } catch (Exception ex) {
                                        // check if the value is of format (-a)
                                        if (rValueOperends[1].trim().endsWith(
                                                ")")
                                                && rValueOperends[1].trim()
                                                .startsWith("(")) {
                                            // retrieve the value
                                            offset = rValueOperends[1]
                                                    .trim()
                                                    .substring(
                                                            1,
                                                            rValueOperends[1]
                                                                    .trim()
                                                                    .length() - 1);
                                            try {
                                                Integer.parseInt(offset);
                                            } catch (Exception exInner) {
                                                // Return the format is not
                                                // supported by the system.
                                                throw new IllegalActionException(
                                                        "Format not supported by the analysis:"
                                                                + exInner
                                                                .getMessage());
                                            }

                                        }

                                    }
                                    // set up all possible transitions
                                    // regarding to this assignment.

                                    String statePrecondition = "state="
                                            + stateInThis.getDisplayName();

                                    _generatePremiseAndResultEachTransition(
                                            signalPremise.toString()
                                            + statePrecondition,
                                            valueDomain, lValue, offset, "/");
                                    _generatePremiseAndResultEachTransition(
                                            signalPremise.toString()
                                            + statePrecondition,
                                            valueDomain, "state",
                                            destinationInThis.getDisplayName(),
                                            "S");

                                } else if (Pattern.matches(".*\\+.*", rValue)) {
                                    String[] rValueOperends = rValue
                                            .split("\\+");

                                    String offset = rValueOperends[1].trim();

                                    try {
                                        Integer.parseInt(rValueOperends[1]
                                                .trim());
                                    } catch (Exception ex) {
                                        // check if the value is of format (-a)
                                        if (rValueOperends[1].trim().endsWith(
                                                ")")
                                                && rValueOperends[1].trim()
                                                .startsWith("(")) {
                                            // retrieve the value
                                            offset = rValueOperends[1]
                                                    .trim()
                                                    .substring(
                                                            1,
                                                            rValueOperends[1]
                                                                    .trim()
                                                                    .length() - 1);
                                            try {
                                                Integer.parseInt(offset);
                                            } catch (Exception exInner) {
                                                // Return the format is not
                                                // supported by the system.
                                                throw new IllegalActionException(
                                                        "SMVUtility.generateAllVariableTransition() clashes:\n"
                                                                + "Format not supported by the system.");
                                            }

                                        }

                                    }
                                    // set up all possible transitions
                                    // regarding to this assignment.

                                    String statePrecondition = "state="
                                            + stateInThis.getDisplayName();
                                    // Check if the value is in the maximum.
                                    // If so, then we need to use "gt".

                                    _generatePremiseAndResultEachTransition(
                                            signalPremise.toString()
                                            + statePrecondition,
                                            valueDomain, lValue,
                                            rValueOperends[1].trim(), "+");

                                    _generatePremiseAndResultEachTransition(
                                            signalPremise.toString()
                                            + statePrecondition,
                                            valueDomain, "state",
                                            destinationInThis.getDisplayName(),
                                            "S");

                                } else if (Pattern.matches(".*\\-.*", rValue)) {

                                    String[] rValueOperends = rValue
                                            .split("\\-");

                                    String offset = rValueOperends[1].trim();

                                    try {
                                        Integer.parseInt(rValueOperends[1]
                                                .trim());
                                    } catch (Exception ex) {
                                        // check if the value is of format
                                        // (-a)
                                        if (rValueOperends[1].trim().endsWith(
                                                ")")
                                                && rValueOperends[1].trim()
                                                .startsWith("(")) {
                                            // retrieve the value
                                            offset = rValueOperends[1]
                                                    .trim()
                                                    .substring(
                                                            1,
                                                            rValueOperends[1]
                                                                    .trim()
                                                                    .length() - 1);
                                            try {
                                                Integer.parseInt(offset);
                                            } catch (Exception exInner) {
                                                throw new RuntimeException(
                                                        " Th return the format \""
                                                                + offset
                                                                + "\" is notsupported by the system.",
                                                                exInner);
                                            }

                                        }

                                    }
                                    // set up all possible transitions
                                    // regarding to this assignment.

                                    String statePrecondition = "state="
                                            + stateInThis.getDisplayName();

                                    _generatePremiseAndResultEachTransition(
                                            signalPremise.toString()
                                            + statePrecondition,
                                            valueDomain, lValue,
                                            rValueOperends[1].trim(), "-");
                                    _generatePremiseAndResultEachTransition(
                                            signalPremise.toString()
                                            + statePrecondition,
                                            valueDomain, "state",
                                            destinationInThis.getDisplayName(),
                                            "S");

                                }

                            }
                        }

                    }
                } else {
                    // Note that there may be no setActions in the
                    // transition.
                    String statePrecondition = "state="
                            + stateInThis.getDisplayName();
                    _generatePremiseAndResultEachTransition(
                            signalPremise.toString() + statePrecondition,
                            valueDomain, "state",
                            destinationInThis.getDisplayName(), "S");
                }

                String outputActionExpression = transition.outputActions
                        .getExpression();
                if (outputActionExpression != null
                        && !outputActionExpression.trim().equals("")) {
                    // Retrieve possible value of the variable
                    String[] splitExpression = outputActionExpression
                            .split(";");
                    for (String element : splitExpression) {
                        String[] characters = element.split("=");
                        String lValue = characters[0].trim();

                        String statePrecondition = "state="
                                + stateInThis.getDisplayName();
                        _generatePremiseAndResultEachTransition(
                                signalPremise.toString() + statePrecondition,
                                valueDomain, lValue + "_isPresent", "1", "N");

                        // MODIFICATION 2008.07.22:
                        // Now we also need to consider the value
                        // of the token. Fortunately, it can be only of two
                        // values: 0 or 1. We throw an exception when the
                        // sending token is not in these two numbers. For the
                        // rest, we can copy the existing code used in the
                        // setAction.

                        if (characters.length >= 1) {
                            String rValue = characters[1].trim();
                            if (Pattern.matches("^-?\\d+$",
                                    characters[1].trim()) == true) {
                                // Generate all possible conditions that leads
                                // to this change.

                                _generatePremiseAndResultEachTransition(
                                        signalPremise.toString()
                                        + statePrecondition,
                                        valueDomain, lValue + "_value", rValue,
                                        "N");
                            }
                        }

                    }
                }
            }

        }

    }

    private static HashSet<String> _generateGraphicalSpecificationRecursiveStep(
            CompositeActor model, String specType,
            StringBuffer upperLevelStatement) throws IllegalActionException {

        HashSet<String> returnSpecificationStateSet = new HashSet<String>();
        // Based on specType, decide the type of the spec we are going to
        // generate.
        boolean isRiskSpec = false;
        if (specType.equalsIgnoreCase("Risk")) {
            isRiskSpec = true;
        }

        // List out all FSMs with their states.
        for (Iterator actors = model.entityList().iterator(); actors.hasNext();) {
            Entity innerEntity = (Entity) actors.next();
            if (innerEntity instanceof FmvAutomaton) {
                // See if there are states that is marked.
                Iterator states = ((FmvAutomaton) innerEntity).entityList()
                        .iterator();
                while (states.hasNext()) {
                    NamedObj state = (NamedObj) states.next();
                    if (state instanceof FmvState) {
                        if (isRiskSpec == true) {
                            if (((BooleanToken) ((FmvState) state).isRiskAnalysisState
                                    .getToken()).booleanValue()) {
                                if (state.getName() != null) {
                                    if (upperLevelStatement.toString().trim()
                                            .equalsIgnoreCase("")) {
                                        returnSpecificationStateSet
                                        .add(innerEntity.getName()
                                                .trim()
                                                + ".state = "
                                                + state.getName());
                                    } else {
                                        returnSpecificationStateSet
                                        .add(upperLevelStatement
                                                .toString().trim()
                                                + "."
                                                + innerEntity.getName()
                                                .trim()
                                                + ".state = "
                                                + state.getName());
                                    }

                                }

                            }
                        } else {
                            if (((BooleanToken) ((FmvState) state).isReachabilityAnalysisState
                                    .getToken()).booleanValue()) {
                                if (state.getName() != null) {
                                    returnSpecificationStateSet
                                    .add(upperLevelStatement.toString()
                                            + innerEntity.getName()
                                            .trim()
                                            + ".state = "
                                            + state.getName());
                                }

                            }
                        }

                    }
                }
            } else if (innerEntity instanceof ModalModel) {
                // We know that it is impossible for a ModalModel to have a
                // state with label. Also it is impossible to have a state
                // machine refinement with label. Thus we may simply traverse
                // the system to see if a state has general refinement.

                FSMActor controller = ((ModalModel) innerEntity)
                        .getController();

                Iterator states = controller.entityList().iterator();
                while (states.hasNext()) {
                    NamedObj state = (NamedObj) states.next();
                    if (state instanceof State) {
                        String refinementList = ((State) state).refinementName
                                .getExpression();
                        if (refinementList == null
                                || refinementList.equalsIgnoreCase("")) {
                            continue;
                        } else {
                            TypedActor[] innerActors = ((State) state)
                                    .getRefinement();
                            if (innerActors != null) {
                                if (innerActors.length == 1) {
                                    TypedActor innerActor = innerActors[0];
                                    if (innerActor instanceof FSMActor) {
                                        continue;
                                    } else if (innerActor instanceof CompositeActor) {
                                        if (upperLevelStatement.toString()
                                                .trim().equalsIgnoreCase("")) {
                                            HashSet<String> subSpecificationStateSet = _generateGraphicalSpecificationRecursiveStep(
                                                    (CompositeActor) innerActor,
                                                    specType,
                                                    upperLevelStatement
                                                    .append(innerEntity
                                                            .getName()
                                                            .trim()));
                                            returnSpecificationStateSet
                                            .addAll(subSpecificationStateSet);
                                        } else {
                                            HashSet<String> subSpecificationStateSet = _generateGraphicalSpecificationRecursiveStep(
                                                    (CompositeActor) innerActor,
                                                    specType,
                                                    upperLevelStatement
                                                    .append("."
                                                            + innerEntity
                                                            .getName()
                                                            .trim()));
                                            returnSpecificationStateSet
                                            .addAll(subSpecificationStateSet);
                                        }

                                    }
                                } else {
                                    throw new IllegalActionException(
                                            "SMVUtility._generateGraphicalSpecificationRecursiveStep() clashes: "
                                                    + "number of inner actors greater than 1 ");
                                }
                            }
                        }
                    }
                }
            }
        }

        return returnSpecificationStateSet;

    }

    /** This function is used to generate detailed pre-conditions and
     * post-conditions in .smv format. It is used by the function
     * _generateAllVariableTransitions()
     *
     * @param statePrecondition
     * @param valueDomain
     * @param lValue
     * @param offset
     * @param operatingSign
     * @exception IllegalActionException
     */
    private static void _generatePremiseAndResultEachTransition(
            String statePrecondition,
            HashMap<String, ArrayList<Integer>> valueDomain, String lValue,
            String offset, String operatingSign) throws IllegalActionException {

        // 1. If operatingSign=="N", then offset means the value that needs to
        // be assigned.
        // 2. if operatingSign=="S", then offset means the destination vertex
        // label.
        // 3. For rest cases (operatingSign=="+","-","*","/"), variable
        // has "X = X operatingSign offset".

        // String[] keySetArray = (String[]) valueDomain.keySet().toArray(
        // new String[0]);
        String[] keySetArray = valueDomain.keySet().toArray(
                new String[valueDomain.keySet().size()]);

        _generatePremiseAndResultEachTransitionRecursiveStep(statePrecondition,
                0, keySetArray.length, keySetArray, valueDomain, lValue,
                offset, operatingSign);

    }

    /**
     * A private function used as a recursive step to generate all premises for
     * enabling transition in .smv file. In variable valueDomain, it specifies
     * that for a particular transition, the set of all possible values to
     * invoke the transition. Thus it is the duty of this recursive step
     * function to generate all possible combinations. The function would try to
     * attach correct premise and update correct new value for the variable set
     * by the transition based on the original value.
     *
     * @param currentPremise
     *                Current precondition for the transition. It is not
     *                completed unless parameter index == maxIndex.
     * @param index
     *                Current depth for the recursive function. It would stop
     *                when it reaches maxIndex.
     * @param maxIndex
     * @param keySetArray
     *                keySetArray stores all variable names that is used in this
     *                transition.
     * @param valueDomain
     *                valueDomain specifies for a particular transition, for
     *                each variable, the set of all possible values to invoke
     *                the transition.
     * @param lValue
     *                lValue specifies the variable name that would be set after
     *                the transition.
     * @param newVariableValue
     *                newVariableValue can have different meanings based on
     *                different value of variable operatingSign. When
     *                operatingSign is +,-,*,/ it represents the offset.
     *                Remember in the set-action, each sub-statement has formats
     *                either <i>var = var operatingSign offset</i> or <i>var =
     *                rValue</i>. When operatingSign is S or N, it represents
     *                the rValue of the system.
     * @param operatingSign
     *
     */
    private static void _generatePremiseAndResultEachTransitionRecursiveStep(
            String currentPremise, int index, int maxIndex,
            String[] keySetArray,
            HashMap<String, ArrayList<Integer>> valueDomain, String lValue,
            String newVariableValue, String operatingSign)
                    throws IllegalActionException {

        if (index >= maxIndex) {
            // MODIFICATION 2008.07.22:
            // if the variable lValue is equal to the type XX_value,
            // check if the new value is 0.
            // if the new value is zero, then we should append a negation to the
            // premise
            boolean b2 = Pattern.matches(".*_value", lValue);
            if (b2 == true && newVariableValue.trim().equalsIgnoreCase("0")) {
                // Store in the array with !(currentPremise)
                VariableTransitionInfo newTransitionInfo = new VariableTransitionInfo();
                newTransitionInfo._preCondition = " !(" + currentPremise + ") ";
                // newTransitionInfo._variableName = lValue;
                newTransitionInfo._variableNewValue = newVariableValue;
                LinkedList<VariableTransitionInfo> temp = _variableTransitionInfo
                        .remove(lValue);
                if (temp == null) {
                    throw new IllegalActionException(
                            "Internal error, removing \"" + lValue
                            + "\" returned null?");
                } else {
                    temp.add(newTransitionInfo);
                    _variableTransitionInfo.put(lValue, temp);
                }
            } else {
                // Store in the array

                VariableTransitionInfo newTransitionInfo = new VariableTransitionInfo();
                newTransitionInfo._preCondition = currentPremise;
                // newTransitionInfo._variableName = lValue;
                newTransitionInfo._variableNewValue = newVariableValue;
                LinkedList<VariableTransitionInfo> temp = _variableTransitionInfo
                        .remove(lValue);
                if (temp == null) {
                    throw new IllegalActionException(
                            "Internal error, removing \"" + lValue
                            + "\" returned null?");
                } else {
                    temp.add(newTransitionInfo);
                    _variableTransitionInfo.put(lValue, temp);
                }
            }

        } else {
            // retrieve all possible variable value in this stage, skip when no
            // possible value is needed.

            // See if this key corresponds to the lValue; if so we need to
            // record the new value of the outcome.
            if (keySetArray[index].equalsIgnoreCase(lValue)) {
                // update the newVariableValue based on +, -, *, /, and N.
                if (operatingSign.equalsIgnoreCase("+")) {
                    // vList stores all possible values for the variable
                    // that is possible to perform transition
                    ArrayList<Integer> vList = valueDomain
                            .get(keySetArray[index]);
                    if (vList != null && vList.size() != 0) {
                        for (int i = 0; i < vList.size(); i++) {

                            // check whether the offset is positive or negative.
                            if (Integer.parseInt(newVariableValue) >= 0) {
                                // Offset positive/zero case (positive_const)

                                if (vList.get(i).intValue() == DOMAIN_GT) {

                                    // newpremise=currentPremise & (var = C)
                                    // String newPremise = new String(
                                    // currentPremise + " & "
                                    // + keySetArray[index] + "="
                                    // + "gt");
                                    String newPremise = currentPremise + " & "
                                            + keySetArray[index] + "=" + "gt";

                                    // When the original value is GT, then
                                    // GT + positive_const = GT
                                    // Hence the updated value remains the same.
                                    _generatePremiseAndResultEachTransitionRecursiveStep(
                                            newPremise, index + 1, maxIndex,
                                            keySetArray, valueDomain, lValue,
                                            "gt", operatingSign);

                                } else if (vList.get(i).intValue() == DOMAIN_LS) {
                                    // For DOMAIN_LS, we place conservative
                                    // analysis and assert that it might lead to
                                    // all its possible values. For example, if
                                    // min=1, and offset=3, then possible value
                                    // may include LS, 1, 2.

                                    String newPremise = currentPremise + " & "
                                            + keySetArray[index] + "=" + "ls";
                                    // String newPremise = new String(
                                    // currentPremise + " & "
                                    // + keySetArray[index] + "="
                                    // + "ls");
                                    // First, LS + positive_const = LS
                                    _generatePremiseAndResultEachTransitionRecursiveStep(
                                            newPremise, index + 1, maxIndex,
                                            keySetArray, valueDomain, lValue,
                                            "ls", operatingSign);
                                    VariableInfo variableInfo = _variableInfo
                                            .get(lValue);
                                    if (variableInfo == null) {
                                        throw new IllegalActionException(
                                                "Internal error, getting \""
                                                        + lValue
                                                        + "\" returned null?");
                                    } else {
                                        if (variableInfo._minValue != null
                                                && variableInfo._maxValue != null) {
                                            int minimumInBoundary = Integer
                                                    .parseInt(variableInfo._minValue);
                                            for (int j = 0; j < Integer
                                                    .parseInt(newVariableValue); j++) {

                                                // We need to make sure that it
                                                // would
                                                // never exceeds upper bound. If
                                                // it
                                                // is below lower bound, we must
                                                // stop it
                                                // and use GT to replace the
                                                // value.

                                                if (minimumInBoundary + j > Integer
                                                        .parseInt(variableInfo._maxValue)) {
                                                    _generatePremiseAndResultEachTransitionRecursiveStep(
                                                            newPremise,
                                                            index + 1,
                                                            maxIndex,
                                                            keySetArray,
                                                            valueDomain,
                                                            lValue, "gt",
                                                            operatingSign);
                                                    break;
                                                }

                                                String updatedVariableValue = String
                                                        .valueOf(minimumInBoundary
                                                                + j);
                                                _generatePremiseAndResultEachTransitionRecursiveStep(
                                                        newPremise, index + 1,
                                                        maxIndex, keySetArray,
                                                        valueDomain, lValue,
                                                        updatedVariableValue,
                                                        operatingSign);
                                            }
                                        }
                                    }
                                } else {
                                    VariableInfo variableInfo = _variableInfo
                                            .get(lValue);
                                    if (variableInfo != null) {
                                        if (variableInfo._maxValue != null) {
                                            // For ordinary cases, we only need
                                            // to check if the new value
                                            // would exceeds the upper
                                            // bound. If so, then use DOMAIN_GT
                                            // to replace the value.

                                            String newPremise = currentPremise
                                                    + " & "
                                                    + keySetArray[index]
                                                            + "="
                                                            + String.valueOf(vList.get(
                                                                    i).intValue());

                                            String updatedVariableValue = String
                                                    .valueOf(vList.get(i)
                                                            .intValue()
                                                            + Integer
                                                            .parseInt(newVariableValue));

                                            if (vList.get(i).intValue()
                                                    + Integer
                                                    .parseInt(newVariableValue) > Integer
                                                    .parseInt(variableInfo._maxValue)) {
                                                // Use DOMAIN_GT to replace the
                                                // value.
                                                updatedVariableValue = "gt";
                                            }

                                            _generatePremiseAndResultEachTransitionRecursiveStep(
                                                    newPremise, index + 1,
                                                    maxIndex, keySetArray,
                                                    valueDomain, lValue,
                                                    updatedVariableValue,
                                                    operatingSign);
                                        }
                                    }

                                }
                            } else {
                                // Offset negative case (negative_const)

                                if (vList.get(i).intValue() == DOMAIN_LS) {

                                    String newPremise = currentPremise + " & "
                                            + keySetArray[index] + "=" + "ls";

                                    // When the original value is LS, then
                                    // LS + negative_const = LS
                                    // Hence the updated value remains the same.
                                    _generatePremiseAndResultEachTransitionRecursiveStep(
                                            newPremise, index + 1, maxIndex,
                                            keySetArray, valueDomain, lValue,
                                            "ls", operatingSign);

                                } else if (vList.get(i).intValue() == DOMAIN_GT) {
                                    String newPremise = currentPremise + " & "
                                            + keySetArray[index] + "=" + "gt";

                                    // When the original value is GT, we place
                                    // conservative analysis and assert that it
                                    // might lead to all its possible values.

                                    // First case: GT + negative_const = GT
                                    _generatePremiseAndResultEachTransitionRecursiveStep(
                                            newPremise, index + 1, maxIndex,
                                            keySetArray, valueDomain, lValue,
                                            "gt", operatingSign);

                                    VariableInfo variableInfo = _variableInfo
                                            .get(lValue);
                                    if (variableInfo == null) {
                                        throw new IllegalActionException(
                                                "Internal error, getting \""
                                                        + lValue
                                                        + "\" returned null?");
                                    } else {
                                        if (variableInfo._minValue != null
                                                && variableInfo._maxValue != null) {
                                            int maximumInBoundary = Integer
                                                    .parseInt(variableInfo._maxValue);
                                            for (int j = 0; j > Integer
                                                    .parseInt(newVariableValue); j--) {
                                                // here j-- because
                                                // newVariableValue is
                                                // negative

                                                // We need to make sure that it
                                                // would
                                                // never exceeds upper bound. If
                                                // it
                                                // is below lower bound, we must
                                                // stop it
                                                // and use LS to replace the
                                                // value.

                                                if (maximumInBoundary + j < Integer
                                                        .parseInt(variableInfo._minValue)) {
                                                    _generatePremiseAndResultEachTransitionRecursiveStep(
                                                            newPremise,
                                                            index + 1,
                                                            maxIndex,
                                                            keySetArray,
                                                            valueDomain,
                                                            lValue, "ls",
                                                            operatingSign);
                                                    break;
                                                }

                                                String updatedVariableValue = String
                                                        .valueOf(maximumInBoundary
                                                                + j);
                                                _generatePremiseAndResultEachTransitionRecursiveStep(
                                                        newPremise, index + 1,
                                                        maxIndex, keySetArray,
                                                        valueDomain, lValue,
                                                        updatedVariableValue,
                                                        operatingSign);
                                            }
                                        }
                                    }
                                } else {
                                    VariableInfo variableInfo = _variableInfo
                                            .get(lValue);
                                    if (variableInfo != null) {
                                        if (variableInfo._minValue != null) {
                                            // For ordinary cases, we only need
                                            // to check
                                            // if the new value would exceeds
                                            // the lower
                                            // bound. If so, then use DOMAIN_LS
                                            // to
                                            // replace the value.

                                            String newPremise = currentPremise
                                                    + " & "
                                                    + keySetArray[index]
                                                            + "="
                                                            + String.valueOf(vList.get(
                                                                    i).intValue());

                                            String updatedVariableValue = String
                                                    .valueOf(vList.get(i)
                                                            .intValue()
                                                            + Integer
                                                            .parseInt(newVariableValue));

                                            if (vList.get(i).intValue()
                                                    + Integer
                                                    .parseInt(newVariableValue) < Integer
                                                    .parseInt(variableInfo._minValue)) {
                                                // Use DOMAIN_LS to replace the
                                                // value.
                                                updatedVariableValue = "ls";
                                            }

                                            _generatePremiseAndResultEachTransitionRecursiveStep(
                                                    newPremise, index + 1,
                                                    maxIndex, keySetArray,
                                                    valueDomain, lValue,
                                                    updatedVariableValue,
                                                    operatingSign);
                                        }
                                    }

                                }
                            }

                        }
                    } else {

                        _generatePremiseAndResultEachTransitionRecursiveStep(
                                currentPremise, index + 1, maxIndex,
                                keySetArray, valueDomain, lValue,
                                newVariableValue, operatingSign);
                    }

                } else if (operatingSign.equalsIgnoreCase("-")) {
                    // Cases when operating sign is minus.

                    ArrayList<Integer> vList = valueDomain
                            .get(keySetArray[index]);

                    if (vList != null && vList.size() != 0) {
                        for (int i = 0; i < vList.size(); i++) {

                            // check whether the offset is positive or negative.
                            if (Integer.parseInt(newVariableValue) >= 0) {
                                // Offset positive/zero case (positive_const)

                                if (vList.get(i).intValue() == DOMAIN_LS) {
                                    // When the original value is LS, then
                                    // LS - positive_const = LS

                                    String newPremise = currentPremise + " & "
                                            + keySetArray[index] + "=" + "ls";

                                    // Hence the updated value remains the same.
                                    _generatePremiseAndResultEachTransitionRecursiveStep(
                                            newPremise, index + 1, maxIndex,
                                            keySetArray, valueDomain, lValue,
                                            "ls", operatingSign);

                                } else if (vList.get(i).intValue() == DOMAIN_GT) {
                                    VariableInfo variableInfo = _variableInfo
                                            .get(lValue);
                                    if (variableInfo != null) {
                                        if (variableInfo._minValue != null
                                                && variableInfo._maxValue != null) {
                                            // If original variable value is GT,
                                            // we
                                            // place conservative analysis and
                                            // assert
                                            // that it might lead to all its
                                            // possible
                                            // values.

                                            String newPremise = currentPremise
                                                    + " & "
                                                    + keySetArray[index] + "="
                                                    + "gt";

                                            // First, it may keep to be GT
                                            _generatePremiseAndResultEachTransitionRecursiveStep(
                                                    newPremise, index + 1,
                                                    maxIndex, keySetArray,
                                                    valueDomain, lValue, "gt",
                                                    operatingSign);

                                            int maximumInBoundary = Integer
                                                    .parseInt(variableInfo._maxValue);
                                            for (int j = 0; j < Integer
                                                    .parseInt(newVariableValue); j++) {

                                                // We need to make sure that it
                                                // would
                                                // never exceeds upper bound. If
                                                // it
                                                // is below lower bound, we must
                                                // stop it
                                                // and use LS to replace the
                                                // value.

                                                if (maximumInBoundary - j < Integer
                                                        .parseInt(variableInfo._minValue)) {
                                                    _generatePremiseAndResultEachTransitionRecursiveStep(
                                                            newPremise,
                                                            index + 1,
                                                            maxIndex,
                                                            keySetArray,
                                                            valueDomain,
                                                            lValue, "ls",
                                                            operatingSign);
                                                    break;
                                                }

                                                String updatedVariableValue = String
                                                        .valueOf(maximumInBoundary
                                                                - j);
                                                _generatePremiseAndResultEachTransitionRecursiveStep(
                                                        newPremise, index + 1,
                                                        maxIndex, keySetArray,
                                                        valueDomain, lValue,
                                                        updatedVariableValue,
                                                        operatingSign);
                                            }
                                        }
                                    }

                                } else {
                                    // For ordinary part, we only need to check
                                    // if the new value would exceed the lower
                                    // bound. If so, then use DOMAIN_LS to
                                    // replace the value.

                                    String newPremise = currentPremise
                                            + " & "
                                            + keySetArray[index]
                                                    + "="
                                                    + String.valueOf(vList.get(i)
                                                            .intValue());

                                    String updatedVariableValue = String
                                            .valueOf(vList.get(i).intValue()
                                                    - Integer
                                                    .parseInt(newVariableValue));
                                    VariableInfo variableInfo = _variableInfo
                                            .get(lValue);
                                    if (variableInfo != null) {
                                        if (variableInfo._minValue != null) {
                                            if (vList.get(i).intValue()
                                                    - Integer
                                                    .parseInt(newVariableValue) < Integer
                                                    .parseInt(variableInfo._minValue)) {
                                                // Use DOMAIN_LS to replace the
                                                // value.
                                                updatedVariableValue = "ls";
                                            }
                                        }
                                    }

                                    _generatePremiseAndResultEachTransitionRecursiveStep(
                                            newPremise, index + 1, maxIndex,
                                            keySetArray, valueDomain, lValue,
                                            updatedVariableValue, operatingSign);
                                }

                            } else {
                                // Offset negative case (negative_const)

                                if (vList.get(i).intValue() == DOMAIN_GT) {
                                    // GT - negative_const = GT

                                    String newPremise = currentPremise + " & "
                                            + keySetArray[index] + "=" + "gt";

                                    // Hence the updated value remains the same.
                                    _generatePremiseAndResultEachTransitionRecursiveStep(
                                            newPremise, index + 1, maxIndex,
                                            keySetArray, valueDomain, lValue,
                                            "gt", operatingSign);

                                } else if (vList.get(i).intValue() == DOMAIN_LS) {
                                    // For DOMAIN_LS, we place conservative
                                    // analysis and assert that it might lead to
                                    // all its possible values

                                    String newPremise = currentPremise + " & "
                                            + keySetArray[index] + "=" + "ls";

                                    // First, LS - negative_const = LS
                                    _generatePremiseAndResultEachTransitionRecursiveStep(
                                            newPremise, index + 1, maxIndex,
                                            keySetArray, valueDomain, lValue,
                                            "ls", operatingSign);

                                    VariableInfo variableInfo = _variableInfo
                                            .get(lValue);
                                    if (variableInfo == null) {
                                        throw new IllegalActionException(
                                                "Internal error, removing \""
                                                        + lValue
                                                        + "\" returned null?");
                                    } else {
                                        if (variableInfo._minValue != null
                                                && variableInfo._maxValue != null) {
                                            int minimumInBoundary = Integer
                                                    .parseInt(variableInfo._minValue);

                                            for (int j = 0; j > Integer
                                                    .parseInt(newVariableValue); j--) {

                                                // We need to make sure that it
                                                // would
                                                // never exceeds upper bound. If
                                                // it
                                                // exceeds upper bound, we must
                                                // stop it
                                                // and use GT to replace the
                                                // value.

                                                if (minimumInBoundary - j < Integer
                                                        .parseInt(variableInfo._maxValue)) {
                                                    _generatePremiseAndResultEachTransitionRecursiveStep(
                                                            newPremise,
                                                            index + 1,
                                                            maxIndex,
                                                            keySetArray,
                                                            valueDomain,
                                                            lValue, "gt",
                                                            operatingSign);
                                                    break;
                                                }

                                                String updatedVariableValue = String
                                                        .valueOf(minimumInBoundary
                                                                - j);

                                                _generatePremiseAndResultEachTransitionRecursiveStep(
                                                        newPremise, index + 1,
                                                        maxIndex, keySetArray,
                                                        valueDomain, lValue,
                                                        updatedVariableValue,
                                                        operatingSign);

                                            }
                                        }
                                    }
                                } else {
                                    VariableInfo variableInfo = _variableInfo
                                            .get(lValue);
                                    if (variableInfo == null) {
                                        throw new IllegalActionException(
                                                "Internal error, removing \""
                                                        + lValue
                                                        + "\" returned null?");
                                    } else {
                                        if (variableInfo._minValue != null
                                                && variableInfo._maxValue != null) {
                                            // For ordinary part, we only need
                                            // to check
                                            // if the new value would exceeds
                                            // the upper
                                            // bound. If so, then use DOMAIN_GT
                                            // to
                                            // replace the value.

                                            String newPremise = currentPremise
                                                    + " & "
                                                    + keySetArray[index]
                                                            + "="
                                                            + String.valueOf(vList.get(
                                                                    i).intValue());

                                            String updatedVariableValue = String
                                                    .valueOf(vList.get(i)
                                                            .intValue()
                                                            - Integer
                                                            .parseInt(newVariableValue));

                                            if (vList.get(i).intValue()
                                                    - Integer
                                                    .parseInt(newVariableValue) > Integer
                                                    .parseInt(variableInfo._maxValue)) {
                                                // Use DOMAIN_LS to replace the
                                                // value.
                                                updatedVariableValue = "gt";
                                            }

                                            _generatePremiseAndResultEachTransitionRecursiveStep(
                                                    newPremise, index + 1,
                                                    maxIndex, keySetArray,
                                                    valueDomain, lValue,
                                                    updatedVariableValue,
                                                    operatingSign);
                                        }
                                    }

                                }
                            }
                        }

                    } else {
                        _generatePremiseAndResultEachTransitionRecursiveStep(
                                currentPremise, index + 1, maxIndex,
                                keySetArray, valueDomain, lValue,
                                newVariableValue, operatingSign);
                    }

                } else if (operatingSign.equalsIgnoreCase("*")) {

                    ArrayList<Integer> vList = valueDomain
                            .get(keySetArray[index]);
                    if (vList != null && vList.size() != 0) {
                        for (int i = 0; i < vList.size(); i++) {

                            // check whether the offset is positive or negative.
                            if (Integer.parseInt(newVariableValue) > 0) {
                                // Positive case (positive_const)

                                if (vList.get(i).intValue() == DOMAIN_GT) {

                                    // newpremise = currentPremise & (var =
                                    // const)
                                    String newPremise = currentPremise + " & "
                                            + keySetArray[index] + "=" + "gt";

                                    VariableInfo variableInfo = _variableInfo
                                            .get(lValue);
                                    if (variableInfo == null) {
                                        throw new IllegalActionException(
                                                "Internal error, getting \""
                                                        + lValue
                                                        + "\" returned null?");
                                    } else {
                                        if (variableInfo._minValue != null
                                                && variableInfo._maxValue != null) {
                                            if (Integer
                                                    .parseInt(variableInfo._maxValue) >= 0) {
                                                // when max>=0, GT *
                                                // positive_const = GT
                                                // Hence the updated value
                                                // remains the
                                                // same.
                                                _generatePremiseAndResultEachTransitionRecursiveStep(
                                                        newPremise, index + 1,
                                                        maxIndex, keySetArray,
                                                        valueDomain, lValue,
                                                        "gt", operatingSign);
                                            } else {
                                                // Integer.parseInt(((VariableInfo)
                                                // _variableInfo.get(lValue))._maxValue)
                                                // < 0
                                                //
                                                // Starting from the upper bound
                                                // + 1,
                                                // +2, +3, +4 ... calculate all
                                                // possible
                                                // values until the new
                                                // set-value is
                                                // greater than GT.
                                                //
                                                // For example, if upper bound
                                                // is -5,
                                                // and if the offset is 2, then
                                                // for
                                                // values in GT that is greater
                                                // or equal
                                                // to -2, the new variable would
                                                // be in
                                                // GT. But if the lower bound is
                                                // -7,
                                                // then we need to replace cases
                                                // that is
                                                // lower to -7. For example,
                                                // -4*2=-8. We
                                                // should use LS to represent
                                                // this
                                                // value.
                                                //
                                                // Also we expect to record one
                                                // LS as
                                                // the new value only. So there
                                                // are
                                                // tricks that needs to be
                                                // applied.

                                                int starter = Integer
                                                        .parseInt(variableInfo._maxValue) + 1;

                                                while (starter
                                                        * Integer
                                                        .parseInt(newVariableValue) <= Integer
                                                        .parseInt(variableInfo._maxValue)) {
                                                    if (starter
                                                            * Integer
                                                            .parseInt(newVariableValue) < Integer
                                                            .parseInt(variableInfo._minValue)
                                                            && (starter + 1)
                                                            * Integer
                                                            .parseInt(newVariableValue) >= Integer
                                                            .parseInt(variableInfo._minValue)) {
                                                        // This IF statement
                                                        // represents
                                                        // tricks mentioned
                                                        // above.
                                                        _generatePremiseAndResultEachTransitionRecursiveStep(
                                                                newPremise,
                                                                index + 1,
                                                                maxIndex,
                                                                keySetArray,
                                                                valueDomain,
                                                                lValue, "ls",
                                                                operatingSign);

                                                    } else if (starter
                                                            * Integer
                                                            .parseInt(newVariableValue) <= Integer
                                                            .parseInt(variableInfo._maxValue)
                                                            && starter
                                                            * Integer
                                                            .parseInt(newVariableValue) >= Integer
                                                            .parseInt(variableInfo._minValue)) {
                                                        String updatedVariableValue = String
                                                                .valueOf(starter
                                                                        * Integer
                                                                        .parseInt(newVariableValue));
                                                        _generatePremiseAndResultEachTransitionRecursiveStep(
                                                                newPremise,
                                                                index + 1,
                                                                maxIndex,
                                                                keySetArray,
                                                                valueDomain,
                                                                lValue,
                                                                updatedVariableValue,
                                                                operatingSign);
                                                    }

                                                    starter++;

                                                }

                                                _generatePremiseAndResultEachTransitionRecursiveStep(
                                                        newPremise, index + 1,
                                                        maxIndex, keySetArray,
                                                        valueDomain, lValue,
                                                        "gt", operatingSign);

                                            }
                                        }
                                    }
                                } else if (vList.get(i).intValue() == DOMAIN_LS) {

                                    String newPremise = currentPremise + " & "
                                            + keySetArray[index] + "=" + "ls";

                                    VariableInfo variableInfo = _variableInfo
                                            .get(lValue);
                                    if (variableInfo == null) {
                                        throw new IllegalActionException(
                                                "Internal error, getting \""
                                                        + lValue
                                                        + "\" returned null?");
                                    } else {
                                        if (variableInfo._minValue != null
                                                && variableInfo._maxValue != null) {
                                            if (Integer
                                                    .parseInt(variableInfo._minValue) <= 0) {
                                                // when min<=0, LS *
                                                // positive_const = LS
                                                // Hence the updated value
                                                // remains the
                                                // same.
                                                _generatePremiseAndResultEachTransitionRecursiveStep(
                                                        newPremise, index + 1,
                                                        maxIndex, keySetArray,
                                                        valueDomain, lValue,
                                                        "ls", operatingSign);
                                            } else {
                                                // Starting from the lower bound
                                                // -1,
                                                // -2, -3, -4 ...
                                                // calculate all possible values
                                                // until
                                                // the value is greater than LS.

                                                int starter = Integer
                                                        .parseInt(variableInfo._minValue) - 1;
                                                while (starter
                                                        * Integer
                                                        .parseInt(newVariableValue) >= Integer
                                                        .parseInt(variableInfo._minValue)) {
                                                    if (starter
                                                            * Integer
                                                            .parseInt(newVariableValue) > Integer
                                                            .parseInt(variableInfo._maxValue)
                                                            && (starter - 1)
                                                            * Integer
                                                            .parseInt(newVariableValue) <= Integer
                                                            .parseInt(variableInfo._maxValue)) {

                                                        _generatePremiseAndResultEachTransitionRecursiveStep(
                                                                newPremise,
                                                                index + 1,
                                                                maxIndex,
                                                                keySetArray,
                                                                valueDomain,
                                                                lValue, "gt",
                                                                operatingSign);

                                                    } else if (starter
                                                            * Integer
                                                            .parseInt(newVariableValue) <= Integer
                                                            .parseInt(variableInfo._maxValue)
                                                            && starter
                                                            * Integer
                                                            .parseInt(newVariableValue) >= Integer
                                                            .parseInt(variableInfo._minValue)) {
                                                        String updatedVariableValue = String
                                                                .valueOf(starter
                                                                        * Integer
                                                                        .parseInt(newVariableValue));
                                                        _generatePremiseAndResultEachTransitionRecursiveStep(
                                                                newPremise,
                                                                index + 1,
                                                                maxIndex,
                                                                keySetArray,
                                                                valueDomain,
                                                                lValue,
                                                                updatedVariableValue,
                                                                operatingSign);
                                                    }

                                                    starter++;

                                                }
                                                _generatePremiseAndResultEachTransitionRecursiveStep(
                                                        newPremise, index + 1,
                                                        maxIndex, keySetArray,
                                                        valueDomain, lValue,
                                                        "ls", operatingSign);

                                            }
                                        }
                                    }
                                } else {
                                    // For ordinary part, we only need to check
                                    // if the new value would exceed the lower
                                    // or upper bound.

                                    String newPremise = currentPremise
                                            + " & "
                                            + keySetArray[index]
                                                    + "="
                                                    + String.valueOf(vList.get(i)
                                                            .intValue());

                                    String updatedVariableValue = String
                                            .valueOf(vList.get(i).intValue()
                                                    * Integer
                                                    .parseInt(newVariableValue));

                                    VariableInfo variableInfo = _variableInfo
                                            .get(lValue);
                                    if (variableInfo == null) {
                                        throw new IllegalActionException(
                                                "Internal error, getting \""
                                                        + lValue
                                                        + "\" returned null?");
                                    } else {
                                        if (variableInfo._minValue != null
                                                && variableInfo._maxValue != null) {
                                            if (vList.get(i).intValue()
                                                    * Integer
                                                    .parseInt(newVariableValue) < Integer
                                                    .parseInt(variableInfo._minValue)) {
                                                // Use DOMAIN_LS to replace the
                                                // value.
                                                updatedVariableValue = "ls";

                                            } else if (vList.get(i).intValue()
                                                    * Integer
                                                    .parseInt(newVariableValue) > Integer
                                                    .parseInt(variableInfo._maxValue)) {
                                                updatedVariableValue = "gt";
                                            }

                                            _generatePremiseAndResultEachTransitionRecursiveStep(
                                                    newPremise, index + 1,
                                                    maxIndex, keySetArray,
                                                    valueDomain, lValue,
                                                    updatedVariableValue,
                                                    operatingSign);
                                        }
                                    }
                                }
                            } else if (Integer.parseInt(newVariableValue) < 0) {
                                // Negative case (negative_const)

                                if (vList.get(i).intValue() == DOMAIN_GT) {

                                    String newPremise = currentPremise + " & "
                                            + keySetArray[index] + "=" + "gt";

                                    VariableInfo variableInfo = _variableInfo
                                            .get(lValue);
                                    if (variableInfo == null) {
                                        throw new IllegalActionException(
                                                "Internal error, getting \""
                                                        + lValue
                                                        + "\" returned null?");
                                    } else {
                                        if (variableInfo._minValue != null
                                                && variableInfo._maxValue != null) {

                                            if (Integer
                                                    .parseInt(variableInfo._maxValue) >= 0) {
                                                // Starting from the upper bound
                                                // + 1,
                                                // +2, +3, +4 ...
                                                // calculate all possible values
                                                // until
                                                // the value is less than LS.
                                                //
                                                // For example, if upper bound =
                                                // 1,
                                                // lower bound = -7, and offset
                                                // = -2,
                                                // then we might have possible
                                                // new
                                                // set-values -4, -6, LS

                                                int starter = Integer
                                                        .parseInt(variableInfo._maxValue) + 1;

                                                while (starter
                                                        * Integer
                                                        .parseInt(newVariableValue) >= Integer
                                                        .parseInt(variableInfo._minValue)) {

                                                    String updatedVariableValue = String
                                                            .valueOf(starter
                                                                    * Integer
                                                                    .parseInt(newVariableValue));
                                                    _generatePremiseAndResultEachTransitionRecursiveStep(
                                                            newPremise,
                                                            index + 1,
                                                            maxIndex,
                                                            keySetArray,
                                                            valueDomain,
                                                            lValue,
                                                            updatedVariableValue,
                                                            operatingSign);

                                                    starter++;
                                                }

                                                _generatePremiseAndResultEachTransitionRecursiveStep(
                                                        newPremise, index + 1,
                                                        maxIndex, keySetArray,
                                                        valueDomain, lValue,
                                                        "ls", operatingSign);

                                            } else if (Integer
                                                    .parseInt(variableInfo._maxValue) < 0) {
                                                // One important thing is that
                                                // we may
                                                // have cases where 0 * const =
                                                // 0.
                                                // Because 0 is in GT, so we
                                                // would have
                                                // new value GT as a choice.

                                                int starter = Integer
                                                        .parseInt(variableInfo._maxValue) + 1;
                                                while (starter
                                                        * Integer
                                                        .parseInt(newVariableValue) >= Integer
                                                        .parseInt(variableInfo._minValue)) {
                                                    if (starter
                                                            * Integer
                                                            .parseInt(newVariableValue) > Integer
                                                            .parseInt(variableInfo._maxValue)
                                                            && (starter + 1)
                                                            * Integer
                                                            .parseInt(newVariableValue) <= Integer
                                                            .parseInt(variableInfo._maxValue)) {

                                                        _generatePremiseAndResultEachTransitionRecursiveStep(
                                                                newPremise,
                                                                index + 1,
                                                                maxIndex,
                                                                keySetArray,
                                                                valueDomain,
                                                                lValue, "gt",
                                                                operatingSign);

                                                    } else if (starter
                                                            * Integer
                                                            .parseInt(newVariableValue) <= Integer
                                                            .parseInt(variableInfo._maxValue)
                                                            && starter
                                                            * Integer
                                                            .parseInt(newVariableValue) >= Integer
                                                            .parseInt(variableInfo._minValue)) {
                                                        String updatedVariableValue = String
                                                                .valueOf(starter
                                                                        * Integer
                                                                        .parseInt(newVariableValue));
                                                        _generatePremiseAndResultEachTransitionRecursiveStep(
                                                                newPremise,
                                                                index + 1,
                                                                maxIndex,
                                                                keySetArray,
                                                                valueDomain,
                                                                lValue,
                                                                updatedVariableValue,
                                                                operatingSign);
                                                    }

                                                    starter++;

                                                }
                                                _generatePremiseAndResultEachTransitionRecursiveStep(
                                                        newPremise, index + 1,
                                                        maxIndex, keySetArray,
                                                        valueDomain, lValue,
                                                        "ls", operatingSign);

                                                // Special case where 0 * const
                                                // = 0
                                                _generatePremiseAndResultEachTransitionRecursiveStep(
                                                        newPremise, index + 1,
                                                        maxIndex, keySetArray,
                                                        valueDomain, lValue,
                                                        "gt", operatingSign);

                                            }
                                        }
                                    }
                                } else if (vList.get(i).intValue() == DOMAIN_LS) {
                                    // (Integer.parseInt(newVariableValue) < 0)
                                    // && original variable value == DOMAIN_LS

                                    String newPremise = currentPremise + " & "
                                            + keySetArray[index] + "=" + "ls";

                                    VariableInfo variableInfo = _variableInfo
                                            .get(lValue);
                                    if (variableInfo == null) {
                                        throw new IllegalActionException(
                                                "Internal error, getting \""
                                                        + lValue
                                                        + "\" returned null?");
                                    } else {
                                        if (variableInfo._minValue != null
                                                && variableInfo._maxValue != null) {
                                            if (Integer
                                                    .parseInt(variableInfo._minValue) <= 0) {
                                                // Starting from the lower bound
                                                // -1,
                                                // -2, -3, -4 ...
                                                // calculate all possible values
                                                // until
                                                // the value is less than GT.
                                                //
                                                // For example, if upper bound =
                                                // 7,
                                                // lower bound = -1, and offset
                                                // = -2,
                                                // then we might have possible
                                                // new
                                                // set-values 4, 6, GT

                                                int starter = Integer
                                                        .parseInt(variableInfo._minValue) - 1;

                                                while (starter
                                                        * Integer
                                                        .parseInt(newVariableValue) <= Integer
                                                        .parseInt(variableInfo._maxValue)) {

                                                    String updatedVariableValue = String
                                                            .valueOf(starter
                                                                    * Integer
                                                                    .parseInt(newVariableValue));
                                                    _generatePremiseAndResultEachTransitionRecursiveStep(
                                                            newPremise,
                                                            index + 1,
                                                            maxIndex,
                                                            keySetArray,
                                                            valueDomain,
                                                            lValue,
                                                            updatedVariableValue,
                                                            operatingSign);

                                                    starter++;
                                                }

                                                _generatePremiseAndResultEachTransitionRecursiveStep(
                                                        newPremise, index + 1,
                                                        maxIndex, keySetArray,
                                                        valueDomain, lValue,
                                                        "gt", operatingSign);

                                            } else if (Integer
                                                    .parseInt(variableInfo._minValue) > 0) {
                                                // One important thing is that
                                                // we may
                                                // have cases where 0 * const =
                                                // 0.
                                                // Because 0 is in LS, so we
                                                // would have
                                                // new value LS as a choice.

                                                int starter = Integer
                                                        .parseInt(variableInfo._minValue) - 1;
                                                while (starter
                                                        * Integer
                                                        .parseInt(newVariableValue) <= Integer
                                                        .parseInt(variableInfo._maxValue)) {
                                                    if (starter
                                                            * Integer
                                                            .parseInt(newVariableValue) < Integer
                                                            .parseInt(variableInfo._minValue)
                                                            && (starter + 1)
                                                            * Integer
                                                            .parseInt(newVariableValue) >= Integer
                                                            .parseInt(variableInfo._minValue)) {

                                                        _generatePremiseAndResultEachTransitionRecursiveStep(
                                                                newPremise,
                                                                index + 1,
                                                                maxIndex,
                                                                keySetArray,
                                                                valueDomain,
                                                                lValue, "ls",
                                                                operatingSign);

                                                    } else if (starter
                                                            * Integer
                                                            .parseInt(newVariableValue) <= Integer
                                                            .parseInt(variableInfo._maxValue)
                                                            && starter
                                                            * Integer
                                                            .parseInt(newVariableValue) >= Integer
                                                            .parseInt(variableInfo._minValue)) {
                                                        String updatedVariableValue = String
                                                                .valueOf(starter
                                                                        * Integer
                                                                        .parseInt(newVariableValue));
                                                        _generatePremiseAndResultEachTransitionRecursiveStep(
                                                                newPremise,
                                                                index + 1,
                                                                maxIndex,
                                                                keySetArray,
                                                                valueDomain,
                                                                lValue,
                                                                updatedVariableValue,
                                                                operatingSign);
                                                    }

                                                    starter++;

                                                }
                                                _generatePremiseAndResultEachTransitionRecursiveStep(
                                                        newPremise, index + 1,
                                                        maxIndex, keySetArray,
                                                        valueDomain, lValue,
                                                        "gt", operatingSign);

                                                // Special case where 0 * const
                                                // = 0
                                                _generatePremiseAndResultEachTransitionRecursiveStep(
                                                        newPremise, index + 1,
                                                        maxIndex, keySetArray,
                                                        valueDomain, lValue,
                                                        "ls", operatingSign);

                                            }
                                        }
                                    }
                                } else {
                                    VariableInfo variableInfo = _variableInfo
                                            .get(lValue);
                                    if (variableInfo == null) {
                                        throw new IllegalActionException(
                                                "Internal error, getting \""
                                                        + lValue
                                                        + "\" returned null?");
                                    } else {
                                        if (variableInfo._minValue != null
                                                && variableInfo._maxValue != null) {
                                            // For ordinary part, we only need
                                            // to check
                                            // if the new value would exceeds
                                            // the upper
                                            // bound. If so, then use DOMAIN_GT
                                            // to
                                            // replace the value.

                                            String newPremise = currentPremise
                                                    + " & "
                                                    + keySetArray[index]
                                                            + "="
                                                            + String.valueOf(vList.get(
                                                                    i).intValue());

                                            String updatedVariableValue = String
                                                    .valueOf(vList.get(i)
                                                            .intValue()
                                                            - Integer
                                                            .parseInt(newVariableValue));

                                            if (vList.get(i).intValue()
                                                    - Integer
                                                    .parseInt(newVariableValue) > Integer
                                                    .parseInt(variableInfo._maxValue)) {
                                                // Use DOMAIN_LS to replace the
                                                // value.
                                                updatedVariableValue = "gt";
                                            }

                                            _generatePremiseAndResultEachTransitionRecursiveStep(
                                                    newPremise, index + 1,
                                                    maxIndex, keySetArray,
                                                    valueDomain, lValue,
                                                    updatedVariableValue,
                                                    operatingSign);
                                        }
                                    }

                                }
                            } else {
                                // Integer.parseInt(newVariableValue)==0
                                // When offset is zero, the result should be
                                // zero. So we only need to check if zero
                                // exceeds the upper bound or is below the lower
                                // bound.

                                String newPremise = currentPremise
                                        + " & "
                                        + keySetArray[index]
                                                + "="
                                                + String.valueOf(vList.get(i)
                                                        .intValue());

                                if (vList.get(i).intValue() == DOMAIN_LS) {
                                    newPremise = currentPremise + " & "
                                            + keySetArray[index] + "=" + "ls";
                                } else if (vList.get(i).intValue() == DOMAIN_GT) {
                                    newPremise = currentPremise + " & "
                                            + keySetArray[index] + "=" + "gt";
                                }

                                String updatedVariableValue = "0";

                                VariableInfo variableInfo = _variableInfo
                                        .get(lValue);
                                if (variableInfo == null) {
                                    throw new IllegalActionException(
                                            "Internal error, getting \""
                                                    + lValue
                                                    + "\" returned null?");
                                } else {
                                    if (variableInfo._minValue != null
                                            && variableInfo._maxValue != null) {
                                        if (0 > Integer
                                                .parseInt(variableInfo._maxValue)) {
                                            // Use DOMAIN_LS to replace the
                                            // value.
                                            updatedVariableValue = "gt";
                                        } else if (0 < Integer
                                                .parseInt(variableInfo._minValue)) {
                                            updatedVariableValue = "ls";
                                        }

                                        _generatePremiseAndResultEachTransitionRecursiveStep(
                                                newPremise, index + 1,
                                                maxIndex, keySetArray,
                                                valueDomain, lValue,
                                                updatedVariableValue,
                                                operatingSign);
                                    }

                                }
                            }
                        }
                    } else {
                        _generatePremiseAndResultEachTransitionRecursiveStep(
                                currentPremise, index + 1, maxIndex,
                                keySetArray, valueDomain, lValue,
                                newVariableValue, operatingSign);
                    }

                } else if (operatingSign.equalsIgnoreCase("/")) {
                    // FIXME: Right now the execution of division is not
                    // implemented.

                    ArrayList<Integer> vList = valueDomain
                            .get(keySetArray[index]);

                    // Do as usual
                    if (vList != null && vList.size() != 0) {
                        for (int i = 0; i < vList.size(); i++) {
                            String updatedVariableValue = String.valueOf(vList
                                    .get(i).intValue()
                                    / Integer.parseInt(newVariableValue));
                            // retrieve the string and concatenate
                            String newPremise = currentPremise + " & "
                                    + keySetArray[index] + "="
                                    + String.valueOf(vList.get(i).intValue());

                            if (vList.get(i).intValue() == DOMAIN_LS) {
                                newPremise = currentPremise + " & "
                                        + keySetArray[index] + "=" + "ls";
                            } else if (vList.get(i).intValue() == DOMAIN_GT) {
                                newPremise = currentPremise + " & "
                                        + keySetArray[index] + "=" + "gt";
                            }

                            _generatePremiseAndResultEachTransitionRecursiveStep(
                                    newPremise, index + 1, maxIndex,
                                    keySetArray, valueDomain, lValue,
                                    updatedVariableValue, operatingSign);
                        }
                    } else {
                        _generatePremiseAndResultEachTransitionRecursiveStep(
                                currentPremise, index + 1, maxIndex,
                                keySetArray, valueDomain, lValue,
                                newVariableValue, operatingSign);
                    }

                } else if (operatingSign.equalsIgnoreCase("N")) {

                    ArrayList<Integer> vList = valueDomain
                            .get(keySetArray[index]);
                    if (vList != null && vList.size() != 0) {
                        boolean b1 = Pattern.matches(".*_isPresent",
                                keySetArray[index].trim());
                        if (b1 == true) {
                            String updatedVariableValue = newVariableValue;
                            String newPremise = currentPremise;

                            _generatePremiseAndResultEachTransitionRecursiveStep(
                                    newPremise, index + 1, maxIndex,
                                    keySetArray, valueDomain, lValue,
                                    updatedVariableValue, operatingSign);
                        } else {
                            boolean b2 = Pattern.matches(".*_value",
                                    keySetArray[index].trim());
                            if (b2 == true && vList.size() == 2) {
                                // See if vList.size() == 2, if so simply
                                // attach XX_isPresent to the value.
                                String[] variable = keySetArray[index].trim()
                                        .split("_value");

                                if (currentPremise.trim().equalsIgnoreCase("")) {
                                    _generatePremiseAndResultEachTransitionRecursiveStep(
                                            currentPremise + variable[0].trim()
                                            + "_isPresent ", index + 1,
                                            maxIndex, keySetArray, valueDomain,
                                            lValue, newVariableValue,
                                            operatingSign);
                                } else {
                                    _generatePremiseAndResultEachTransitionRecursiveStep(
                                            currentPremise + " & "
                                                    + variable[0].trim()
                                                    + "_isPresent ", index + 1,
                                                    maxIndex, keySetArray, valueDomain,
                                                    lValue, newVariableValue,
                                                    operatingSign);
                                }
                            } else if (b2 == true) {
                                // Since now we have XX_value, we should
                                // also attach the premise XX_isPresent
                                String[] variable = keySetArray[index].trim()
                                        .split("_value");

                                for (int i = 0; i < vList.size(); i++) {
                                    String updatedVariableValue = newVariableValue;
                                    // retrieve the string and concatenate
                                    String newPremise = currentPremise
                                            + "&"
                                            + keySetArray[index]
                                                    + "="
                                                    + String.valueOf(vList.get(i)
                                                            .intValue()) + "&"
                                                            + variable[0].trim()
                                                            + "_isPresent ";

                                    if (vList.get(i).intValue() == DOMAIN_LS) {
                                        newPremise = currentPremise + " & "
                                                + keySetArray[index] + "="
                                                + "ls" + "&"
                                                + variable[0].trim()
                                                + "_isPresent ";
                                    } else if (vList.get(i).intValue() == DOMAIN_GT) {
                                        newPremise = currentPremise + " & "
                                                + keySetArray[index] + "="
                                                + "gt" + "&"
                                                + variable[0].trim()
                                                + "_isPresent ";
                                    }
                                    _generatePremiseAndResultEachTransitionRecursiveStep(
                                            newPremise, index + 1, maxIndex,
                                            keySetArray, valueDomain, lValue,
                                            updatedVariableValue, operatingSign);
                                }
                            } else {
                                for (int i = 0; i < vList.size(); i++) {
                                    String updatedVariableValue = newVariableValue;
                                    // retrieve the string and concatenate
                                    String newPremise = currentPremise
                                            + " & "
                                            + keySetArray[index]
                                                    + "="
                                                    + String.valueOf(vList.get(i)
                                                            .intValue());

                                    if (vList.get(i).intValue() == DOMAIN_LS) {
                                        newPremise = currentPremise + " & "
                                                + keySetArray[index] + "="
                                                + "ls";
                                    } else if (vList.get(i).intValue() == DOMAIN_GT) {
                                        newPremise = currentPremise + " & "
                                                + keySetArray[index] + "="
                                                + "gt";
                                    }
                                    _generatePremiseAndResultEachTransitionRecursiveStep(
                                            newPremise, index + 1, maxIndex,
                                            keySetArray, valueDomain, lValue,
                                            updatedVariableValue, operatingSign);
                                }
                            }

                        }

                    } else {
                        _generatePremiseAndResultEachTransitionRecursiveStep(
                                currentPremise, index + 1, maxIndex,
                                keySetArray, valueDomain, lValue,
                                newVariableValue, operatingSign);
                    }

                }

            } else {
                // meaning: if
                // (keySetArray[index].equalsIgnoreCase(lValue)==false)

                ArrayList<Integer> vList = valueDomain.get(keySetArray[index]);

                if (vList != null && vList.size() != 0) {
                    // if the keySetArray[index] is similar to "XX_isPresent",
                    // skip the update of premise.
                    boolean b = Pattern.matches(".*_isPresent",
                            keySetArray[index].trim());
                    if (b == true) {
                        _generatePremiseAndResultEachTransitionRecursiveStep(
                                currentPremise, index + 1, maxIndex,
                                keySetArray, valueDomain, lValue,
                                newVariableValue, operatingSign);
                    } else {
                        boolean b2 = Pattern.matches(".*_value",
                                keySetArray[index].trim());
                        if (b2 == true && vList.size() == 2) {

                            _generatePremiseAndResultEachTransitionRecursiveStep(
                                    currentPremise, index + 1, maxIndex,
                                    keySetArray, valueDomain, lValue,
                                    newVariableValue, operatingSign);

                        } else if (b2 == true) {
                            String[] variable = keySetArray[index].trim()
                                    .split("_value");
                            for (int i = 0; i < vList.size(); i++) {
                                // retrieve the string and concatenate
                                String newPremise = currentPremise
                                        + " & "
                                        + keySetArray[index]
                                                + "="
                                                + String.valueOf(vList.get(i)
                                                        .intValue()) + " & "
                                                        + variable[0].trim() + "_isPresent ";

                                if (vList.get(i).intValue() == DOMAIN_LS) {
                                    newPremise = currentPremise + " & "
                                            + keySetArray[index] + "=" + "ls"
                                            + " & " + variable[0].trim()
                                            + "_isPresent ";
                                } else if (vList.get(i).intValue() == DOMAIN_GT) {
                                    newPremise = currentPremise + " & "
                                            + keySetArray[index] + "=" + "gt"
                                            + " & " + variable[0].trim()
                                            + "_isPresent ";
                                }
                                _generatePremiseAndResultEachTransitionRecursiveStep(
                                        newPremise, index + 1, maxIndex,
                                        keySetArray, valueDomain, lValue,
                                        newVariableValue, operatingSign);
                            }
                        } else {
                            for (int i = 0; i < vList.size(); i++) {
                                // retrieve the string and concatenate
                                String newPremise = currentPremise
                                        + " & "
                                        + keySetArray[index]
                                                + "="
                                                + String.valueOf(vList.get(i)
                                                        .intValue());

                                if (vList.get(i).intValue() == DOMAIN_LS) {
                                    newPremise = currentPremise + " & "
                                            + keySetArray[index] + "=" + "ls";
                                } else if (vList.get(i).intValue() == DOMAIN_GT) {
                                    newPremise = currentPremise + " & "
                                            + keySetArray[index] + "=" + "gt";
                                }
                                _generatePremiseAndResultEachTransitionRecursiveStep(
                                        newPremise, index + 1, maxIndex,
                                        keySetArray, valueDomain, lValue,
                                        newVariableValue, operatingSign);
                            }

                        }

                    }

                } else {
                    _generatePremiseAndResultEachTransitionRecursiveStep(
                            currentPremise, index + 1, maxIndex, keySetArray,
                            valueDomain, lValue, newVariableValue,
                            operatingSign);
                }
            }

        }

    }

    /**
     * This function generates the Kripke structure acceptable by NuSMV from
     * a ModalModel in Ptolemy II. Here modular approach is applied to
     * eliminate complexity of the conversion process.
     *
     * @param modalmodel ModalModel under analysis
     * @param span The size to expand the original domain
     * @param upperStateName
     */
    private static ArrayList<StringBuffer> _generateSMVDescriptionModalModelWithRefinement(
            ModalModel modalmodel, String span, String upperStateName)
                    throws IllegalActionException, NameDuplicationException {

        /* The sketch of the algorithm is roughly as follows:
         *
         * (Step 0) All signals has been detected prior to execute this function.
         * First scan from top, for each FSMActor generate its modular description.
         * If an actor is a ModalModel, we try to scan through each state.
         * In a ModalModel, the transition may consist of two different types:
         *
         * (1)reset = true: This means that after this transition, the state of
         *         refinement machine the would be reset to initial state.
         *    Two views exist:
         *    [i] In a global view, states in the destination-state refinement machine
         *        and those in the source-state refinement machine are exclusive,
         *        To represent the state space, we may simply use the union of
         *        each refinement state machine as the total state space.
         *    [ii] The FSM goes back to initial state. Thus we view the whole
         *         ModalModel as the composition of state system where at any
         *         instant, there is one state existing in each refinement.
         *         To represent the state space, we may simply use the product of
         *         each refinement state machine as the total state space.
         *
         *   (2)reset = false: This means that after this transition, state of
         *          refinement machine the would not be reset to initial state.
         *          To represent the state space, we may simply use the product of
         *          each refinement state machine as the total state space.
         *          (similar to [ii])
         *
         *   To sum up, for systems with "history transitions", the representation
         *   requires more state variables to encode.
         *
         *
         * Also for ModalModels, it may have two kinds of state refinements:
         * (a)StateMachineRefinement: The inner refinement is a state machine.
         * (b)GeneralRefinement: This means that the inner refinement is another
         *                       system. (it should also be SR, otherwise is
         *                       beyond our scope for implementation)
         * (c)No refinement
         *
         * Now we list out all possible combinations:
         * (1a): This can be done easily by a whole rewriting of the system into
         *       a bigger FSM consisting all sub-states and possible connections.
         * (1b): This case is extremely complicated: thus we do not allow end
         *       user to operate in this way.
         * (1ab): Same as (1b)
         * (1*c): apply similar concept as (1a,1b,1ab)
         * (2a*): This is contradictory; it is impossible to have a bigger FSM
         *        having two states existing simultaneously.
         * (2b): In this case we build up different submodules for each
         *       refinement, and construct the another FSMcontroller to send
         *       signals accepted by ModalModel to it.
         *
         * FIXME: We only implement cases where transition reset is false;
         * we are currently implementing the case for (2b) and (2a[ii])
         * We may extend the functionality later on.
         *
         * When writing the description of a module, one challenging problem is
         * to understand the location of the signal. For a certain signal required
         * in the transition in the subsystem S'' in S, it may be passed from
         * another system S'. However, because the subsystem S'' is an instance
         * of the system S, S'' can not access the signal of S'.
         *
         * Instead, we need to pass the signal from S' to S, then assign the signal
         * to S'' during the instantiation process of S''.
         *
         * In the description of _globalSignalDistributionInfo and
         *  _globalSignalRetrievalInfo, it only tells you the signal needed for
         * this component, and the signal generated by this component. Thus
         * additional work must be done.
         *
         */
        ArrayList<StringBuffer> modularDescription = new ArrayList<StringBuffer>();
        FSMActor controller = modalmodel.getController();
        controller.setName(modalmodel.getName());

        boolean isStateMachineRefinementInLayer = false;
        boolean isGeneralRefinementInLayer = false;

        Iterator states = controller.entityList().iterator();
        while (states.hasNext()) {
            NamedObj state = (NamedObj) states.next();
            if (state instanceof State) {
                String refinementList = ((State) state).refinementName
                        .getExpression();
                if (refinementList == null
                        || refinementList.equalsIgnoreCase("")) {
                    continue;
                } else {
                    TypedActor[] actors = ((State) state).getRefinement();
                    if (actors != null) {
                        if (actors.length == 1) {
                            TypedActor innerActor = actors[0];
                            if (innerActor instanceof FSMActor) {
                                isStateMachineRefinementInLayer = true;
                            } else if (innerActor instanceof CompositeActor) {
                                isGeneralRefinementInLayer = true;
                            }
                        } else {
                            throw new IllegalActionException(
                                    "SMVUtility._generateSMVFormatModalModelWithRefinement clashes: "
                                            + "number of actors in refinement greater than 1 ");
                        }
                    }
                }
            }
        }
        if (isStateMachineRefinementInLayer == false
                && isGeneralRefinementInLayer == false) {
            // This means that there is no refinement in this ModalModel
            // We simply take out the controller to perform the generation
            // process (the controller is an FSMActor).
            //
            modularDescription.add(_translateSingleFSMActor(controller, span,
                    false, upperStateName));

        } else {
            // This means that it uses general refinement.
            Iterator newStates = controller.entityList().iterator();
            while (newStates.hasNext()) {
                NamedObj state = (NamedObj) newStates.next();
                if (state instanceof State) {
                    String refinementList = ((State) state).refinementName
                            .getExpression();
                    if (refinementList == null
                            || refinementList.equalsIgnoreCase("")) {

                    } else {
                        TypedActor[] actors = ((State) state).getRefinement();
                        if (actors != null) {
                            if (actors.length == 1) {
                                // It would only have the case where
                                // actor.length == 1.
                                // If we encounter cases > 1, report error for
                                // further bug fix.
                                TypedActor innerActor = actors[0];
                                if (innerActor instanceof FSMActor) {
                                    // Here we also need to feed in
                                    // (1) name of the controller
                                    // (2) name of the state

                                    modularDescription
                                    .add(_translateSingleFSMActor(
                                            (FSMActor) innerActor,
                                            span, false,
                                            state.getName()));
                                } else if (innerActor instanceof CompositeActor) {
                                    // First see if its director is SR.
                                    // If not, then it is beyond our current
                                    // processing scope.
                                    Director director = ((CompositeActor) innerActor)
                                            .getDirector();
                                    if (!(director instanceof SRDirector)) {
                                        // This is not what we can process.
                                        throw new IllegalActionException(
                                                "SMVUtility._generateSMVFormatModalModelWithRefinement(): "
                                                        + "Inner director not SR.");
                                    } else {
                                        // This is OK for our analysis
                                        // Generate system description for these
                                        // two.
                                        modularDescription
                                        .add(_generateSMVDescriptionSubSystem(
                                                (CompositeActor) innerActor,
                                                span, state.getName()));

                                    }
                                } else {
                                    // We are not able to deal with it.
                                }
                            } else {
                                // Theoretically this should not happen.
                                // Once this happens, report an error to
                                // notify the author the situation.
                                throw new IllegalActionException(
                                        "SMVUtility._generateSMVFormatModalModelWithRefinement(): "
                                                + "Refinement has two or more inner actors.");
                            }
                        }
                    }
                }
            }

            // Last, generate the Finite State Controller to mediate
            // the system. We can retrieve the controller directly from
            // the ModalModel, but the conversion is different.
            //
            // Now we need to set up each state in controller as a variable.
            // As we switch from state to state, the state variable would be
            // set to zero or one. Each subsystem derived from the state
            // refinement would need to attach the variable whether the state
            // variable is true or not in its transition.

            StringBuffer controllerDescription = _translateSingleFSMActor(
                    controller, span, true, "");
            if (controllerDescription != null) {
                modularDescription.add(controllerDescription);
            }

        }

        return modularDescription;

    }

    /**
     * This private function generates the system description of a
     * subsystem which has a ModalModel controller as its upper-layer.
     *
     * @param model The subsystem which is the refinement of a state.
     * @param span The size of span to expand the domain of variable.
     * @param upperStateName The name of the upper level model name. This upper
     *        state has the model as refinement.
     * @return The StringBuffer description of the subsystem acceptable by the
     *         model checker.
     * @exception IllegalActionException
     * @exception NameDuplicationException
     */
    private static StringBuffer _generateSMVDescriptionSubSystem(
            CompositeActor model, String span, String upperStateName)
                    throws IllegalActionException, NameDuplicationException {

        StringBuffer returnFmvFormat = new StringBuffer("");

        // List out all FSMs with their states.
        for (Iterator actors = model.entityList().iterator(); actors.hasNext();) {
            Entity innerEntity = (Entity) actors.next();
            if (innerEntity instanceof FSMActor) {
                // Directly generate the whole description of the system.
                returnFmvFormat.append(_translateSingleFSMActor(
                        (FSMActor) innerEntity, span, false, upperStateName));
            } else if (innerEntity instanceof ModalModel) {
                // Perform analysis of the ModalModel
                ArrayList<StringBuffer> subSystemDescription = _generateSMVDescriptionModalModelWithRefinement(
                        (ModalModel) innerEntity, span, upperStateName);
                for (int i = 0; i < subSystemDescription.size(); i++) {
                    returnFmvFormat.append(subSystemDescription.get(i));
                }
            }
        }

        return returnFmvFormat;
    }

    /**
     * Perform a systematic pre-scan to obtain information regarding the
     * visibility of a signal. See the description in the source code for
     * technical details.
     *
     * @param model The whole system under analysis
     * @param span The number to expand the domain of a variable. Note that
     *                it is in fact irrelevant to the signal generation. It is
     *                only used for the reuse of existing functions.
     * @return An array of Strings that contains the pre-scan results.
     * @exception IllegalActionException
     * @exception NameDuplicationException
     */
    private static ArrayList<String> _prescanSystemSignal(CompositeActor model,
            String span) throws IllegalActionException,
            NameDuplicationException {

        // This utility function performs a system pre-scanning and stores
        // all signal information in three global variables:
        //
        // (1) HashMap<String, ArrayList<String>> _globalSignalDistributionInfo:
        // It tells you for a certain component, the set of signals
        // used in its guard expression.
        //
        // (2)HashMap<String, HashSet<String>> _globalSignalRetrievalInfo:
        // It tells you for a certain component, the set of signals emitted
        // from that component.
        //
        // (3) HashMap<String, HashSet<String>> _globalSignalNestedRetrievalInfo:
        // It tells you for a certain component, the set of signals emitted
        // from that component and from subsystems below that component in
        // the overall hierarchy.
        //
        // Thus if these three are established, we are able to retrieve
        // the location of the signal.
        //

        ArrayList<String> subSystemNameList = new ArrayList<String>();

        for (Iterator actors = model.entityList().iterator(); actors.hasNext();) {
            Entity innerEntity = (Entity) actors.next();
            if (innerEntity instanceof FSMActor) {
                FSMActor innerFSMActor = (FSMActor) innerEntity;

                // Enumerate all variables used in the Kripke structure
                int numSpan = Integer.parseInt(span);
                HashSet<String> variableSet = null;
                variableSet = _decideStateVariableSet(innerFSMActor, numSpan);

                // Decide variables encoded in the Kripke Structure.
                // Note that here the variable only contains Signal Variables.
                // For example, PGo_isPresent
                //
                // MODIFICATION 2008.07.21:
                // Now we also contain variables like PGo_value for representing
                // the value of the signal variable.
                HashSet<String> signalVariableSet = null;

                // Enumerate all variables used in the Kripke structure
                signalVariableSet = _decideSignalVariableSet(innerFSMActor);

                // Meanwhile, place elements in signalVariableSet into
                // variableSet;
                Iterator<String> itSignalVariableSet = signalVariableSet
                        .iterator();
                while (itSignalVariableSet.hasNext()) {
                    String valName = itSignalVariableSet.next();
                    variableSet.add(valName);
                }
                HashSet<String> signalOfferedSet = new HashSet<String>();
                Iterator<String> newItVariableSet = variableSet.iterator();
                while (newItVariableSet.hasNext()) {
                    String valName = newItVariableSet.next();
                    boolean b1 = Pattern.matches(".*_isPresent", valName);
                    if (b1 == true) {
                        signalOfferedSet.add(valName);
                    }
                    boolean b2 = Pattern.matches(".*_value", valName);
                    if (b2 == true) {
                        signalOfferedSet.add(valName);
                    }
                }

                _globalSignalRetrievalInfo.put(innerFSMActor.getName(),
                        signalOfferedSet);
                _globalSignalNestedRetrievalInfo.put(innerFSMActor.getName(),
                        signalOfferedSet);
                subSystemNameList.add(innerFSMActor.getName());

                HashSet<String> guardSignalVariableSet = null;
                // Enumerate all variables used in the Kripke structure
                guardSignalVariableSet = _decideGuardSignalVariableSet(innerFSMActor);

                ArrayList<String> guardSignalVariableInfo = new ArrayList<String>();

                Iterator<String> itGuardSignalVariableSet = guardSignalVariableSet
                        .iterator();
                while (itGuardSignalVariableSet.hasNext()) {
                    String valName = itGuardSignalVariableSet.next();
                    guardSignalVariableInfo.add(valName);
                }

                _globalSignalDistributionInfo.put(innerFSMActor.getName(),
                        guardSignalVariableInfo);

            } else if (innerEntity instanceof ModalModel) {

                ArrayList<String> subsubSystemNameList = new ArrayList<String>();

                // If innerEntity is an instance of a ModalModel,
                // we need to perform a recursive scan of the
                // signal in the lower level.
                //
                // This includes the controller, and all rest FSMActors
                // or ModalModels in the state refinement.

                FSMActor controller = ((ModalModel) innerEntity)
                        .getController();
                controller.setName(innerEntity.getName());
                Iterator states = controller.entityList().iterator();

                while (states.hasNext()) {
                    NamedObj state = (NamedObj) states.next();
                    if (state instanceof State) {
                        String refinementList = ((State) state).refinementName
                                .getExpression();
                        if (refinementList == null
                                || refinementList.equalsIgnoreCase("")) {
                        } else {
                            // There is refinement in this state
                            TypedActor[] refinementSystemActors = ((State) state)
                                    .getRefinement();
                            if (refinementSystemActors != null) {
                                if (refinementSystemActors.length == 1) {
                                    // It would only have the case where
                                    // actor.length == 1.
                                    // If we encounter cases > 1, report error
                                    // for further bug fix.
                                    TypedActor innerActor = refinementSystemActors[0];
                                    if (innerActor instanceof FSMActor) {
                                        FSMActor innerFSMActor = (FSMActor) innerActor;
                                        HashSet<String> variableSet = null;

                                        // Enumerate all variables used in the
                                        // Kripke structure
                                        int numSpan = Integer.parseInt(span);
                                        variableSet = _decideStateVariableSet(
                                                innerFSMActor, numSpan);

                                        // Decide signal variables encoded in
                                        // the Kripke structure
                                        // For example, PGo_isPresent, PGo_value
                                        HashSet<String> signalVariableSet = null;
                                        signalVariableSet = _decideSignalVariableSet(innerFSMActor);

                                        // Meanwhile, place elements in
                                        // signalVariableSet into variableSet;
                                        Iterator<String> itSignalVariableSet = signalVariableSet
                                                .iterator();
                                        while (itSignalVariableSet.hasNext()) {
                                            String valName = itSignalVariableSet
                                                    .next();
                                            variableSet.add(valName);
                                        }
                                        HashSet<String> signalOfferedSet = new HashSet<String>();
                                        if (variableSet != null) {
                                            Iterator<String> newItVariableSet = variableSet
                                                    .iterator();
                                            while (newItVariableSet.hasNext()) {
                                                String valName = newItVariableSet
                                                        .next();
                                                boolean b1 = Pattern
                                                        .matches(
                                                                ".*_isPresent",
                                                                valName);
                                                if (b1 == true) {
                                                    signalOfferedSet
                                                    .add(valName);
                                                }
                                                boolean b2 = Pattern.matches(
                                                        ".*_value", valName);
                                                if (b2 == true) {
                                                    signalOfferedSet
                                                    .add(valName);
                                                }

                                            }
                                        }

                                        _globalSignalRetrievalInfo.put(
                                                innerFSMActor.getName().trim(),
                                                signalOfferedSet);
                                        _globalSignalNestedRetrievalInfo.put(
                                                innerFSMActor.getName().trim(),
                                                signalOfferedSet);
                                        subsubSystemNameList.add(innerFSMActor
                                                .getName().trim());

                                        HashSet<String> guardSignalVariableSet = null;
                                        // Enumerate all variables used in the
                                        // Kripke structure
                                        guardSignalVariableSet = _decideGuardSignalVariableSet(innerFSMActor);

                                        ArrayList<String> guardSignalVariableInfo = new ArrayList<String>();

                                        Iterator<String> itGuardSignalVariableSet = guardSignalVariableSet
                                                .iterator();
                                        while (itGuardSignalVariableSet
                                                .hasNext()) {
                                            String valName = itGuardSignalVariableSet
                                                    .next();
                                            guardSignalVariableInfo
                                            .add(valName);

                                        }

                                        _globalSignalDistributionInfo.put(
                                                innerFSMActor.getName(),
                                                guardSignalVariableInfo);

                                    } else if (innerActor instanceof CompositeActor) {
                                        // First see if its director is SR.
                                        // If not, then it is beyond our current
                                        // processing scope.
                                        Director director = ((CompositeActor) innerActor)
                                                .getDirector();
                                        if (!(director instanceof SRDirector)) {
                                            // This is not what we can process.
                                            throw new IllegalActionException(
                                                    "SMVUtility._prescanSystemSignal() clashes:\n"
                                                            + "Inner director not SR.");
                                        } else {
                                            // This is OK for our analysis
                                            // Generate system description
                                            // for these two.

                                            ArrayList<String> subsubsubSystemNameList = _prescanSystemSignal(
                                                    (CompositeActor) innerActor,
                                                    span);
                                            for (int j = 0; j < subsubsubSystemNameList
                                                    .size(); j++) {
                                                subsubSystemNameList
                                                .add(subsubsubSystemNameList
                                                        .get(j));
                                            }

                                        }
                                    } else {
                                        // We are not able to deal with it.
                                        // Simply omit those cases.
                                    }

                                } else {
                                    // Theoretically this should not happen.
                                    // Once this happens, report an error to
                                    // notify the author the situation.
                                    throw new IllegalActionException(
                                            "SMVUtility._prescanSystemSignal() clashes: \n"
                                                    + "Refinement has two or more inner actors.");
                                }
                            }
                        }
                    }
                }

                // Lastly, store the signal information for the controller.

                HashSet<String> variableSet = null;
                // Enumerate all variables used in the Kripke structure
                int numSpan = Integer.parseInt(span);
                variableSet = _decideStateVariableSet(controller, numSpan);

                // Decide variables encoded in the Kripke Structure.
                // Note that here the variable only contains Signal Variables.
                // For example, PGo_isPresent
                HashSet<String> signalVariableSet = null;
                signalVariableSet = _decideSignalVariableSet(controller);

                // Meanwhile, place elements in signalVariableSet into
                // variableSet;
                Iterator<String> itSignalVariableSet = signalVariableSet
                        .iterator();
                while (itSignalVariableSet.hasNext()) {

                    String valName = itSignalVariableSet.next();
                    variableSet.add(valName);
                }
                HashSet<String> signalOfferedSet = new HashSet<String>();
                Iterator<String> newItVariableSet = variableSet.iterator();
                while (newItVariableSet.hasNext()) {
                    String valName = newItVariableSet.next();
                    boolean b1 = Pattern.matches(".*_isPresent", valName);
                    if (b1 == true) {
                        signalOfferedSet.add(valName);
                    }
                    boolean b2 = Pattern.matches(".*_value", valName);
                    if (b2 == true) {
                        signalOfferedSet.add(valName);
                    }

                }

                // Use a new HashSet and copy all contents to avoid later
                // modification.
                HashSet<String> newSignalOfferedSet = new HashSet<String>();
                if (signalOfferedSet != null) {
                    newSignalOfferedSet.addAll(signalOfferedSet);
                }
                _globalSignalRetrievalInfo.put(controller.getName(),
                        newSignalOfferedSet);

                // Now retrieve every subComponent s' of s from
                // subsubSystemNameList,
                // retrieve the signal from _globalSignalRetrievalInfo(s')
                // and add them to _globalSignalNestedRetrievalInfo(s)
                for (int i = 0; i < subsubSystemNameList.size(); i++) {
                    String component = subsubSystemNameList.get(i);
                    HashSet<String> componentSignalSet = _globalSignalNestedRetrievalInfo
                            .get(component);

                    if (componentSignalSet != null) {
                        signalOfferedSet.addAll(componentSignalSet);
                    }
                    subSystemNameList.add(component);
                }

                if (_globalSignalNestedRetrievalInfo.get(controller.getName()) != null) {
                    _globalSignalNestedRetrievalInfo.get(controller.getName())
                    .addAll(signalOfferedSet);

                } else {
                    _globalSignalNestedRetrievalInfo.put(controller.getName(),
                            signalOfferedSet);

                }

                HashSet<String> guardSignalVariableSet = null;
                guardSignalVariableSet = _decideGuardSignalVariableSet(controller);

                ArrayList<String> guardSignalVariableInfo = new ArrayList<String>();

                Iterator<String> itGuardSignalVariableSet = guardSignalVariableSet
                        .iterator();
                while (itGuardSignalVariableSet.hasNext()) {
                    String valName = itGuardSignalVariableSet.next();
                    guardSignalVariableInfo.add(valName);
                }

                _globalSignalDistributionInfo.put(controller.getName(),
                        guardSignalVariableInfo);

                subSystemNameList.add(controller.getName());

            } else if (innerEntity instanceof SMVLegacyCodeActor) {
                HashSet<String> signalOfferedSet = new HashSet<String>();
                // Retrieve the port from the actor.
                Iterator<Port> itOutPortList = ((SMVLegacyCodeActor) innerEntity)
                        .outputPortList().iterator();
                while (itOutPortList.hasNext()) {
                    String portName = itOutPortList.next().getName();
                    signalOfferedSet.add(portName.trim() + "_isPresent");
                    signalOfferedSet.add(portName.trim() + "_value");
                }

                if (_globalSignalNestedRetrievalInfo.get(innerEntity.getName()) != null) {
                    _globalSignalNestedRetrievalInfo.get(innerEntity.getName())
                    .addAll(signalOfferedSet);
                } else {
                    _globalSignalNestedRetrievalInfo.put(innerEntity.getName(),
                            signalOfferedSet);
                }

                ArrayList<String> guardSignalVariableInfo = new ArrayList<String>();
                Iterator<Port> itInPortList = ((SMVLegacyCodeActor) innerEntity)
                        .inputPortList().iterator();

                while (itInPortList.hasNext()) {
                    String portName = itInPortList.next().getName();
                    guardSignalVariableInfo.add(portName.trim() + "_isPresent");
                    guardSignalVariableInfo.add(portName.trim() + "_value");

                }

                _globalSignalDistributionInfo.put(innerEntity.getName(),
                        guardSignalVariableInfo);

            } else if (innerEntity instanceof CompositeActor) {
                // FIXME: No implementation here; this corresponds to
                // the first fix-me statement.
            }

        }
        return subSystemNameList;

    }

    /**
     * This function is trying to generate the definition for modules contained
     * in a controller. It need to check whether a signal is visible by the
     * controller. If not, then this signal should be passed from outside, and
     * the required signal set (extended by guard signals) for the controller
     * should add up this signal. If a signal is visible by the controller, then
     * we know that this signal is only passed between modules of the
     * controller. We can list out the location of the signal.
     *
     * @param controller The controller which contains those modules
     * @return An ArrayList containing all submodule definitions
     * @exception IllegalActionException Undefined behavior happens.
     */
    private static ArrayList<StringBuffer> _retrieveSubSystemModuleNameParameterInfo(
            FSMActor controller) throws IllegalActionException {

        // One important modification is that we need to see if the signal is
        // exchanged between subsystems of a certain controller.
        // Because a subsystem can not see the signal outside the system,
        // thus if a subsystem really uses a signal from outside, we
        // need to add up the signal name (without the position) as an invoker
        // for example subModule (Sec_isPresent)
        //

        ArrayList<StringBuffer> returnList = new ArrayList<StringBuffer>();
        Iterator states = controller.entityList().iterator();
        while (states.hasNext()) {
            NamedObj state = (NamedObj) states.next();
            if (state instanceof State) {
                String refinementList = ((State) state).refinementName
                        .getExpression();
                if (refinementList == null
                        || refinementList.equalsIgnoreCase("")) {
                    continue;
                } else {
                    TypedActor[] actors = ((State) state).getRefinement();
                    if (actors != null) {
                        if (actors.length == 1) {
                            // It would only have the case where
                            // actor.length == 1.
                            // If we encounter cases > 1, report error for
                            // further bug fix.
                            TypedActor innerActor = actors[0];
                            if (innerActor instanceof FSMActor) {
                                StringBuffer moduleDescription = new StringBuffer(
                                        "");
                                moduleDescription.append("\t\t"
                                        + innerActor.getName() + ": "
                                        + innerActor.getName() + "(");
                                // Check if the variable has variable outside
                                // (invisible
                                // in the whole system); where is the location.
                                // We use two variables to indicate the
                                // possibilities:
                                // (1) containInTheSystem: Signal visible in the
                                // system
                                // (2) containInTheModule: Signal visible by
                                // module
                                ArrayList<String> signalInfo = _globalSignalDistributionInfo
                                        .get(innerActor.getName());
                                if (signalInfo != null) {
                                    for (int i = 0; i < signalInfo.size(); i++) {
                                        String signalName = signalInfo.get(i);
                                        boolean containInTheSystem = false;
                                        boolean containInTheModule = false;
                                        String location = "";
                                        Iterator<String> it = _globalSignalRetrievalInfo
                                                .keySet().iterator();
                                        while (it.hasNext()) {
                                            String place = it.next();

                                            if (_globalSignalRetrievalInfo
                                                    .get(place) != null) {
                                                if (_globalSignalRetrievalInfo
                                                        .get(place).contains(
                                                                signalName)) {
                                                    location = place;
                                                    containInTheSystem = true;
                                                    break;
                                                }
                                            }

                                        }

                                        // Now we need to see whether this
                                        // signal
                                        // is within the scope of the
                                        // controller.
                                        // If the signal is not in the module,
                                        // we set the variable
                                        // containInTheModule=false
                                        if (_globalSignalNestedRetrievalInfo
                                                .get(controller.getName()) != null) {
                                            if (_globalSignalNestedRetrievalInfo
                                                    .get(controller.getName())
                                                    .contains(signalName)) {
                                                containInTheModule = true;
                                            }

                                        }

                                        if (containInTheSystem == true) {
                                            if (containInTheModule == true) {
                                                if (i == signalInfo.size() - 1) {
                                                    moduleDescription
                                                    .append(location
                                                            .trim()
                                                            + "."
                                                            + signalName
                                                            + " ");
                                                } else {
                                                    moduleDescription
                                                    .append(location
                                                            .trim()
                                                            + "."
                                                            + signalName
                                                            + ", ");
                                                }
                                            } else {
                                                if (_globalSignalDistributionInfo
                                                        .get(controller
                                                                .getName()) != null) {
                                                    if (_globalSignalDistributionInfo
                                                            .get(controller
                                                                    .getName())
                                                                    .contains(
                                                                            signalName) == false) {
                                                        _globalSignalDistributionInfo
                                                        .get(controller
                                                                .getName())
                                                                .add(signalName);
                                                    }
                                                }

                                                if (i == signalInfo.size() - 1) {
                                                    moduleDescription
                                                    .append(signalName
                                                            + " ");
                                                } else {
                                                    moduleDescription
                                                    .append(signalName
                                                            + ", ");
                                                }
                                            }

                                        } else {
                                            // use 1 to represent the signal
                                            if (i == signalInfo.size() - 1) {
                                                moduleDescription.append(" 1");
                                            } else {
                                                moduleDescription.append(" 1,");
                                            }
                                        }
                                    }
                                }
                                // Add up the state as parameter because
                                // these subsystems are controlled by the state
                                // of the controller.

                                if (signalInfo != null && signalInfo.size() > 0) {
                                    moduleDescription.append(", state );\n");
                                } else {
                                    moduleDescription.append(" state );\n");
                                }

                                returnList.add(moduleDescription);

                            } else if (innerActor instanceof CompositeActor) {
                                // First see if its director is SR.
                                // If not, then it is beyond our current
                                // processing scope.
                                Director director = ((CompositeActor) innerActor)
                                        .getDirector();
                                if (!(director instanceof SRDirector)) {
                                    // This is not what we can process.
                                    throw new IllegalActionException(
                                            "SMVUtility._retrieveSubSystemModuleNameParameterInfo(): "
                                                    + "Inner director not SR.");
                                } else {
                                    // The general case, we need to list out
                                    // all modules in the lower level
                                    // (one layer lower only)

                                    for (Iterator innerInnerActors = ((CompositeActor) innerActor)
                                            .entityList().iterator(); innerInnerActors
                                            .hasNext();) {
                                        StringBuffer moduleDescription = new StringBuffer(
                                                "");
                                        Entity innerInnerEntity = (Entity) innerInnerActors
                                                .next();
                                        // Entity innerModel = innerEntity;
                                        if (innerInnerEntity instanceof FSMActor) {
                                            moduleDescription.append("\t\t"
                                                    + innerInnerEntity
                                                    .getName()
                                                    + ": "
                                                    + innerInnerEntity
                                                    .getName() + "(");
                                            // Check if the variable has
                                            // variable outside; where is the
                                            // location.
                                            ArrayList<String> signalInfo = _globalSignalDistributionInfo
                                                    .get(innerInnerEntity
                                                            .getName());

                                            if (signalInfo != null) {
                                                for (int i = 0; i < signalInfo
                                                        .size(); i++) {
                                                    String signalName = signalInfo
                                                            .get(i);
                                                    boolean containInTheSystem = false;
                                                    boolean containInTheModule = false;
                                                    String location = "";
                                                    Iterator<String> it = _globalSignalRetrievalInfo
                                                            .keySet()
                                                            .iterator();
                                                    while (it.hasNext()) {
                                                        String place = it
                                                                .next();
                                                        if (_globalSignalRetrievalInfo
                                                                .get(place) != null) {
                                                            if (_globalSignalRetrievalInfo
                                                                    .get(place)
                                                                    .contains(
                                                                            signalName)) {
                                                                location = place;
                                                                containInTheSystem = true;
                                                                break;
                                                            }
                                                        }

                                                    }
                                                    if (_globalSignalNestedRetrievalInfo
                                                            .get(controller
                                                                    .getName()) != null) {
                                                        if (_globalSignalNestedRetrievalInfo
                                                                .get(controller
                                                                        .getName())
                                                                        .contains(
                                                                                signalName) == true) {
                                                            containInTheModule = true;
                                                        }
                                                    }

                                                    if (containInTheSystem == true) {
                                                        if (containInTheModule == true) {
                                                            if (i == signalInfo
                                                                    .size() - 1) {
                                                                moduleDescription
                                                                .append(location
                                                                        .trim()
                                                                        + "."
                                                                        + signalName
                                                                        + ", ");
                                                            } else {
                                                                moduleDescription
                                                                .append(location
                                                                        .trim()
                                                                        + "."
                                                                        + signalName
                                                                        + ", ");
                                                            }
                                                        } else {
                                                            if (_globalSignalDistributionInfo
                                                                    .get(controller
                                                                            .getName()) != null) {
                                                                if (_globalSignalDistributionInfo
                                                                        .get(controller
                                                                                .getName())
                                                                                .contains(
                                                                                        signalName) == false) {
                                                                    _globalSignalDistributionInfo
                                                                    .get(controller
                                                                            .getName())
                                                                            .add(signalName);
                                                                }
                                                            }

                                                            if (i == signalInfo
                                                                    .size() - 1) {
                                                                moduleDescription
                                                                .append(signalName
                                                                        + ", ");
                                                            } else {
                                                                moduleDescription
                                                                .append(signalName
                                                                        + ", ");
                                                            }
                                                        }

                                                    } else {
                                                        // use 1 to represent
                                                        // the signal
                                                        if (i == signalInfo
                                                                .size() - 1) {
                                                            moduleDescription
                                                            .append(" 1, state");
                                                        } else {
                                                            moduleDescription
                                                            .append(" 1,");
                                                        }
                                                    }
                                                }
                                            }
                                            if (signalInfo != null
                                                    && signalInfo.size() > 0) {
                                                moduleDescription
                                                .append(" , state );\n");
                                            } else {
                                                moduleDescription
                                                .append(" state );\n");
                                            }

                                            returnList.add(moduleDescription);

                                        } else if (innerInnerEntity instanceof ModalModel) {
                                            moduleDescription.append("\t\t"
                                                    + innerInnerEntity
                                                    .getName()
                                                    + ": "
                                                    + innerInnerEntity
                                                    .getName() + "(");
                                            // Check if the variable has
                                            // variable outside;
                                            // where is the location.
                                            ArrayList<String> signalInfo = _globalSignalDistributionInfo
                                                    .get(innerInnerEntity
                                                            .getName());

                                            if (signalInfo != null) {
                                                for (int i = 0; i < signalInfo
                                                        .size(); i++) {
                                                    String signalName = signalInfo
                                                            .get(i);
                                                    boolean containInTheSystem = false;
                                                    boolean containInTheModule = false;
                                                    String location = "";
                                                    Iterator<String> it = _globalSignalRetrievalInfo
                                                            .keySet()
                                                            .iterator();
                                                    while (it.hasNext()) {
                                                        String place = it
                                                                .next();
                                                        if (_globalSignalRetrievalInfo
                                                                .get(place)
                                                                .contains(
                                                                        signalName)) {
                                                            location = place;
                                                            containInTheSystem = true;
                                                            break;
                                                        }
                                                    }
                                                    if (_globalSignalNestedRetrievalInfo
                                                            .get(controller
                                                                    .getName()) != null) {
                                                        if (_globalSignalNestedRetrievalInfo
                                                                .get(controller
                                                                        .getName())
                                                                        .contains(
                                                                                signalName) == true) {
                                                            containInTheModule = true;
                                                        }
                                                    }

                                                    if (containInTheSystem == true) {
                                                        if (containInTheModule == true) {
                                                            if (i == signalInfo
                                                                    .size() - 1) {
                                                                moduleDescription
                                                                .append(location
                                                                        .trim()
                                                                        + "."
                                                                        + signalName
                                                                        + " ");
                                                            } else {
                                                                moduleDescription
                                                                .append(location
                                                                        .trim()
                                                                        + "."
                                                                        + signalName
                                                                        + ", ");
                                                            }
                                                        } else {
                                                            if (_globalSignalDistributionInfo
                                                                    .get(controller
                                                                            .getName()) != null) {
                                                                if (_globalSignalDistributionInfo
                                                                        .get(controller
                                                                                .getName())
                                                                                .contains(
                                                                                        signalName) == false) {
                                                                    _globalSignalDistributionInfo
                                                                    .get(controller
                                                                            .getName())
                                                                            .add(signalName);
                                                                }
                                                            }

                                                            if (i == signalInfo
                                                                    .size() - 1) {
                                                                moduleDescription
                                                                .append(signalName
                                                                        + " ");
                                                            } else {
                                                                moduleDescription
                                                                .append(signalName
                                                                        + ", ");
                                                            }
                                                        }

                                                    } else {
                                                        // use 1 to represent
                                                        // the signal
                                                        if (i == signalInfo
                                                                .size() - 1) {
                                                            moduleDescription
                                                            .append(" 1");
                                                        } else {
                                                            moduleDescription
                                                            .append(" 1,");
                                                        }
                                                    }
                                                }
                                            }
                                            if (signalInfo != null
                                                    && signalInfo.size() > 0) {
                                                moduleDescription
                                                .append(", state );\n");
                                            } else {
                                                moduleDescription
                                                .append(" state );\n");
                                            }
                                            returnList.add(moduleDescription);
                                        }
                                    }

                                }
                            } else {
                                // We are not able to deal with it.
                                // Simply skip without doing anything
                            }

                        } else {
                            // Theoretically this should not happen.
                            // Once this happens, report an error to
                            // notify the author the situation.
                            throw new IllegalActionException(
                                    "SMVUtility._retrieveSubSystemModuleNameParameterInfo(): "
                                            + "Refinement has two or more inner actors.");
                        }
                    }
                }
            }
        }

        return returnList;
    }

    /**
     * A private function used as to generate variable initial values for the
     * initial variable set. The current approach is to retrieve from the
     * parameter specified in the actor.
     *
     * @param actor The actor under analysis
     * @param variableSet Set of variables that expect to find initial values.
     * @return A HashMap indicating the pair (variable name, initial value).
     */
    private static HashMap<String, String> _retrieveVariableInitialValue(
            FSMActor actor, HashSet<String> variableSet)
                    throws IllegalActionException {

        // One problem regarding the initial value retrieval from parameters
        // is that when retrieving parameters, the return value would consist
        // of some undesirable information. We need to use "split" to do further
        // analysis.

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
                // See if the property is of the format "XX_isPresent".
                // If so, then
                boolean b1 = Pattern.matches(".*_isPresent", attribute.trim());
                // See if it is the case of XX_value
                boolean b2 = Pattern.matches(".*_value", attribute.trim());
                if (b1 == false && b2 == false) {
                    throw new IllegalActionException(
                            "The initial value of the variable \"" + attribute
                            + " is unspecified in the parameter.");
                }
            }

            // Retrieve the value of the variable. Property contains
            // a huge trunk of string content, and only the last variable is
            // useful.

            returnMap.put(attribute, property);
        }
        return returnMap;

    }

    /**
     * Translate a single FSMActor into the format acceptable by model
     * checker. New functionalities for supporting boolean-token
     * recognition are added.
     *
     * @param actor The FSMActor under analysis.
     * @param span The constant span for expanding the integer domain for state (inner) variables
     * @param isController Whether the FSMActor is the controller of a ModalModel
     * @param refinementStateName The name of the refinement state
     * @return The translated actor in a format suitable for the model checker.
     * @exception IllegalActionException
     */
    private static StringBuffer _translateSingleFSMActor(FSMActor actor,
            String span, boolean isController, String refinementStateName)
                    throws IllegalActionException {

        // The utility function translates a single
        // FSMActor into formats acceptable by model checker NuSMV.
        //
        // (1) A single FSMActor can be the controller of the ModalModel,
        // then in the description we need to instantiate each of the
        // sub-models (which is generated by state refinement) contained
        // within.
        //
        // (2) A single FSMActor can also be the a component of the subsystem
        // (state refinement with general model). In this way, we need to
        // add up information whether the current state in the upper
        // controller is the state holding this FSMActor.
        //
        // If refinementStateName is not an empty string, then we know that
        // we need to be sure that the upper state must be in the state
        // "refinementStateName", otherwise, it is not allowed to perform
        // any transition.
        //

        String refinementStateActivePremise = "UpperState = "
                + refinementStateName.trim();

        StringBuffer returnSmvFormat = new StringBuffer("");
        returnSmvFormat.append("\tVAR \n");

        if (isController == true) {
            // For a controller, it also needs to instantiate all of the
            // inner modules existed below. Thus we use a function
            // _retrieveSubSystemModuleNameParameterInfo to retrieve
            // modules in the lower level.
            ArrayList<StringBuffer> subModules = _retrieveSubSystemModuleNameParameterInfo(actor);
            for (int i = 0; i < subModules.size(); i++) {
                returnSmvFormat.append(subModules.get(i));
                returnSmvFormat.append("\n ");
            }
        }

        returnSmvFormat.append("\t\tstate : {");

        // Enumerate all states in the automaton
        HashSet<State> frontier = null;
        frontier = _enumerateStateSet(actor);

        // Print out all these states
        Iterator<State> it = frontier.iterator();
        while (it.hasNext()) {
            State val = it.next();
            returnSmvFormat.append(val.getDisplayName());
            if (it.hasNext()) {
                returnSmvFormat.append(",");
            }
        }
        returnSmvFormat.append("};\n");

        // Decide variables encoded in the Kripke Structure.
        // Note that here the variable only contains inner variables.
        HashSet<String> variableSet = null;

        // Enumerate all variables used in the Kripke structure
        int numSpan = Integer.parseInt(span);
        variableSet = _decideStateVariableSet(actor, numSpan);

        Iterator<String> itVariableSet = variableSet.iterator();
        while (itVariableSet.hasNext()) {

            String valName = itVariableSet.next();
            returnSmvFormat.append("\t\t" + valName + " : {");
            // Retrieve the lower bound and upper bound of the variable used in
            // the system based on inequalities or assignments
            // Also, add up symbols "ls" and "gt" within the variable domain.
            VariableInfo individual = _variableInfo.get(valName);
            if (individual == null) {
                throw new IllegalActionException(
                        "Error in SMVUtility.translateSingleFSMActor(): \n_variableInfo.get(valName) == null?");
            } else {
                if (individual._minValue != null
                        && individual._maxValue != null) {
                    int lowerBound = Integer.parseInt(individual._minValue);
                    int upperBound = Integer.parseInt(individual._maxValue);

                    returnSmvFormat.append(" ls,");
                    for (int number = lowerBound; number <= upperBound; number++) {
                        returnSmvFormat.append(number);
                        returnSmvFormat.append(",");
                    }
                    returnSmvFormat.append("gt };\n");
                }
            }
        }

        // Decide variables encoded in the Kripke Structure.
        // Note that here the variable only contains Signal Variables.
        // For example, PGo_isPresent and PGo_value

        HashSet<String> signalVariableSet = null;
        signalVariableSet = _decideSignalVariableSet(actor);

        // Meanwhile, place elements in signalVariableSet into variableSet;
        if (signalVariableSet != null) {
            Iterator<String> itSignalVariableSet = signalVariableSet.iterator();
            while (itSignalVariableSet.hasNext()) {
                String valName = itSignalVariableSet.next();
                variableSet.add(valName);
            }
        }

        // Now start the variable transition calculation process.
        // First we would perform on states.

        returnSmvFormat.append("\tASSIGN \n");

        // setup initial state
        String name = actor.getInitialState().getName();
        returnSmvFormat.append("\t\tinit(state) := " + name + ";\n");

        _generateAllVariableTransitions(actor, variableSet);

        returnSmvFormat.append("\t\tnext(state) :=\n");
        returnSmvFormat.append("\t\t\tcase\n");

        // Generate all transitions; start from "state"
        LinkedList<VariableTransitionInfo> infoList = _variableTransitionInfo
                .get("state");
        if (infoList != null) {
            for (int i = 0; i < infoList.size(); i++) {
                VariableTransitionInfo info = infoList.get(i);
                // MODIFICATION FOR THE ACTOR WHICH IS PART OF THE SUBSYSTEM
                // UNDER A REFINEMENT OF A STATE.
                if (refinementStateName.equalsIgnoreCase("")) {
                    returnSmvFormat.append("\t\t\t\t" + info._preCondition
                            + " :{ " + info._variableNewValue + " };\n");
                } else {
                    returnSmvFormat.append("\t\t\t\t"
                            + refinementStateActivePremise + " & "
                            + info._preCondition + " :{ "
                            + info._variableNewValue + " };\n");
                }
            }
        }
        // nusmv 2.5.4 requires "TRUE" instead of "1" here.
        returnSmvFormat.append("\t\t\t\tTRUE             : state;\n");
        returnSmvFormat.append("\t\t\tesac;\n\n");

        // Find out initial values for those variables.
        HashMap<String, String> variableInitialValue;
        variableInitialValue = _retrieveVariableInitialValue(actor, variableSet);

        // Generate all transitions; run for every variable used in
        // Kripke structure.
        Iterator<String> newItVariableSet = variableSet.iterator();
        while (newItVariableSet.hasNext()) {

            String valName = newItVariableSet.next();
            boolean b1 = Pattern.matches(".*_isPresent", valName);
            boolean b2 = Pattern.matches(".*_value", valName);
            if (b1 == true || b2 == true) {

            } else {
                returnSmvFormat.append("\t\tinit(" + valName + ") := "
                        + variableInitialValue.get(valName) + ";\n");
                returnSmvFormat.append("\t\tnext(" + valName + ") :=\n");
                returnSmvFormat.append("\t\t\tcase\n");

                // Generate all transitions; start from "state"
                List<VariableTransitionInfo> innerInfoList = _variableTransitionInfo
                        .get(valName);
                if (innerInfoList != null) {
                    for (int i = 0; i < innerInfoList.size(); i++) {
                        VariableTransitionInfo info = innerInfoList.get(i);
                        // MODIFICATION FOR THE ACTOR WHICH IS PART OF THE
                        // SUBSYSTEM UNDER A REFINEMENT OF A STATE.
                        if (refinementStateName.equalsIgnoreCase("")) {
                            returnSmvFormat.append("\t\t\t\t"
                                    + info._preCondition + " :{ "
                                    + info._variableNewValue + " };\n");
                        } else {
                            returnSmvFormat.append("\t\t\t\t"
                                    + refinementStateActivePremise + " & "
                                    + info._preCondition + " :{ "
                                    + info._variableNewValue + " };\n");
                        }

                    }
                }
                // nusmv 2.5.4 requires "TRUE" instead of "1" here.
                returnSmvFormat.append("\t\t\t\tTRUE             : " + valName
                        + ";\n");

                returnSmvFormat.append("\t\t\tesac;\n\n");
            }
        }

        // Lastly, attach the name and parameter required to use in the system.
        // In our current implementation, it corresponds to those variables
        // having format "XX_isPresent" in guard expression.

        // Decide variables encoded in the Kripke Structure.
        // Note that here the variable only contains Signal Variables.
        // For example, PGo_isPresent

        StringBuffer frontAttachment = new StringBuffer("\nMODULE "
                + actor.getName() + "( ");

        ArrayList<String> guardSignalVariableInfo = _globalSignalDistributionInfo
                .get(actor.getName());
        if (guardSignalVariableInfo == null) {
            HashSet<String> guardSignalVariableSet = null;
            // Enumerate all variables used in the Kripke structure
            guardSignalVariableSet = _decideGuardSignalVariableSet(actor);

            Iterator<String> itGuardSignalVariableSet = guardSignalVariableSet
                    .iterator();
            while (itGuardSignalVariableSet.hasNext()) {
                String valName = itGuardSignalVariableSet.next();
                // guardSignalVariableInfo.add(valName);
                if (itGuardSignalVariableSet.hasNext() == true) {
                    frontAttachment.append(valName + ",");
                } else {
                    frontAttachment.append(valName);
                }
            }
        } else {
            for (int i = 0; i < guardSignalVariableInfo.size(); i++) {
                String valName = guardSignalVariableInfo.get(i);
                if (i != guardSignalVariableInfo.size() - 1) {
                    frontAttachment.append(valName + ",");
                } else {
                    frontAttachment.append(valName);
                }
            }

        }

        // MODIFICATION FOR THE ACTOR WHICH IS PART OF THE SUBSYSTEM
        // UNDER A REFINEMENT OF A STATE.
        if (refinementStateName.trim().equalsIgnoreCase("")) {
            frontAttachment.append(" )\n");
        } else {
            // Coverity suggests that guardSignalVariableInfo might be null.
            if (guardSignalVariableInfo == null
                    || guardSignalVariableInfo.size() == 0) {
                frontAttachment.append(" UpperState  )\n");
            } else {
                frontAttachment.append(", UpperState )\n");
            }

        }

        frontAttachment.append(returnSmvFormat);

        if (signalVariableSet != null) {
            if (signalVariableSet.size() != 0) {
                frontAttachment.append("\n\tDEFINE\n");
                Iterator<String> newItSignalVariableSet = signalVariableSet
                        .iterator();
                while (newItSignalVariableSet.hasNext()) {
                    String valName = newItSignalVariableSet.next();
                    frontAttachment.append("\t\t" + valName + " := ");

                    List<VariableTransitionInfo> innerInfoList = _variableTransitionInfo
                            .get(valName);
                    if (innerInfoList != null) {
                        for (int i = 0; i < innerInfoList.size(); i++) {
                            VariableTransitionInfo info = innerInfoList.get(i);
                            // MODIFICATION FOR THE ACTOR WHICH IS PART OF THE
                            // SUBSYSTEM UNDER A REFINEMENT OF A STATE.
                            if (i == 0) {
                                if (i == innerInfoList.size() - 1) {
                                    if (refinementStateName
                                            .equalsIgnoreCase("")) {
                                        frontAttachment.append(" ( "
                                                + info._preCondition
                                                + " ) ;\n\n  ");
                                    } else {
                                        frontAttachment.append(" ("
                                                + refinementStateActivePremise
                                                + " & " + info._preCondition
                                                + " ) ;\n\n  ");
                                    }
                                } else {
                                    if (refinementStateName
                                            .equalsIgnoreCase("")) {
                                        frontAttachment.append(" ( "
                                                + info._preCondition + " )  ");
                                    } else {
                                        frontAttachment.append(" ("
                                                + refinementStateActivePremise
                                                + " & " + info._preCondition
                                                + " )  ");
                                    }
                                }

                            } else if (i == innerInfoList.size() - 1) {
                                if (info._preCondition.contains("!")) {
                                    if (refinementStateName
                                            .equalsIgnoreCase("")) {
                                        frontAttachment.append(" & ( "
                                                + info._preCondition
                                                + " ) ;\n\n ");
                                    } else {
                                        frontAttachment.append(" & ("
                                                + refinementStateActivePremise
                                                + " & " + info._preCondition
                                                + " ) ;\n\n ");
                                    }
                                } else {
                                    if (refinementStateName
                                            .equalsIgnoreCase("")) {
                                        frontAttachment.append(" | ( "
                                                + info._preCondition
                                                + " ) ;\n\n ");
                                    } else {
                                        frontAttachment.append(" | ("
                                                + refinementStateActivePremise
                                                + " & " + info._preCondition
                                                + " ) ;\n\n ");
                                    }
                                }
                            } else {
                                if (info._preCondition.contains("!")) {
                                    if (refinementStateName
                                            .equalsIgnoreCase("")) {
                                        frontAttachment.append(" & ( "
                                                + info._preCondition + " )  ");
                                    } else {
                                        frontAttachment.append(" & ("
                                                + refinementStateActivePremise
                                                + " & " + info._preCondition
                                                + " ) ");
                                    }
                                } else {
                                    if (refinementStateName
                                            .equalsIgnoreCase("")) {
                                        frontAttachment.append(" | ( "
                                                + info._preCondition + " )  ");
                                    } else {
                                        frontAttachment.append(" | ("
                                                + refinementStateActivePremise
                                                + " & " + info._preCondition
                                                + " ) ");
                                    }
                                }

                            }

                        }
                    }

                }
            }

        }

        return frontAttachment;

    }

    private static HashMap<String, ArrayList<String>> _globalSignalDistributionInfo;
    private static HashMap<String, HashSet<String>> _globalSignalRetrievalInfo;
    private static HashMap<String, HashSet<String>> _globalSignalNestedRetrievalInfo;

    private static HashMap<String, VariableInfo> _variableInfo;
    private static HashMap<String, LinkedList<VariableTransitionInfo>> _variableTransitionInfo;

    private static int DOMAIN_GT = Integer.MAX_VALUE;
    private static int DOMAIN_LS = Integer.MIN_VALUE;

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
    private static class VariableTransitionInfo {
        private VariableTransitionInfo() {
        }

        private String _preCondition;
        // Record set of conditions that leads to the change of variable.
        private String _variableNewValue = null;
    }

}
