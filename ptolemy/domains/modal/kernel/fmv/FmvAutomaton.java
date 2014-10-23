/* An FSM supporting verification using formal methods.

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
package ptolemy.domains.modal.kernel.fmv;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import ptolemy.domains.modal.kernel.FSMActor;
import ptolemy.domains.modal.kernel.State;
import ptolemy.domains.modal.kernel.Transition;
import ptolemy.kernel.ComponentPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;
import ptolemy.verification.kernel.MathematicalModelConverter.FormulaType;

///////////////////////////////////////////////////////////////////
//// FmvAutomaton

/**
 * A Formal Method Verification (FMV) Automaton. An FmvAutomaton is not
 * different from a regular FSM, but the class definition provides a
 * specialized environment to convert into format acceptable by model
 * checker NuSMV. Also, the state insertion of FmvAutomaton supports the
 * inserting of FmvState, where these specialized states are able to
 * have property indicating whether it is a risk state.
 *
 * @deprecated ptolemy.de.lib.TimedDelay is deprecated, use ptolemy.actor.lib.TimeDelay.
 * @author Chihhong Patrick Cheng, Contributor: Edward A. Lee
 * @version $Id$
 * @since Ptolemy II 8.0
 * @Pt.ProposedRating Red (patrickj)
 * @Pt.AcceptedRating Red (patrickj)
 * @see ptolemy.domains.modal.kernel.State
 * @see ptolemy.domains.modal.kernel.Transition
 * @see ptolemy.domains.modal.kernel.fmv.FmvState
 */

@Deprecated
public class FmvAutomaton extends FSMActor {
    /**
     * Construct an FmvAutomaton in the default workspace with an empty string
     * as its name. Add the actor to the workspace directory. Increment the
     * version number of the workspace.
     */
    public FmvAutomaton() {
        super();
    }

    /**
     * Construct an FmvAutomaton in the specified workspace with an empty string
     * as its name. The name can be changed later with setName(). If the
     * workspace argument is null, then use the default workspace. Add the actor
     * to the workspace directory. Increment the version number of the
     * workspace.
     *
     * @param workspace The workspace that will list the actor.
     */
    public FmvAutomaton(Workspace workspace) {
        super(workspace);
    }

    /**
     * Create an FmvAutomaton in the specified container with the specified
     * name. The name must be unique within the container or an exception is
     * thrown. The container argument must not be null, or a
     * NullPointerException will be thrown.
     *
     * @param container The container.
     * @param name The name of this automaton within the container.
     * @exception IllegalActionException
     *                    If the entity cannot be contained by the proposed
     *                    container.
     * @exception NameDuplicationException
     *                    If the name coincides with an entity already in the
     *                    container.
     */
    public FmvAutomaton(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    /**
     * Return an StringBuffer that contains the .smv format of the
     * FmvAutomaton.
     *
     * @param formula The temporal formula used to be attached in the
     * .smv file.

     * @param choice The type of the formula. It may be either a CTL or LTL
     *               formula.
     * @param span A constant used to expand the size of the rough domain.
     * @return The .smv format of the FmvAutomaton.
     * @exception IllegalActionException If there is a problem with
     * the conversion.
     */
    public StringBuffer convertToSMVFormat(String formula, FormulaType choice,
            int span) throws IllegalActionException {

        _variableInfo = new HashMap<String, VariableInfo>();

        StringBuffer returnSmvFormat = new StringBuffer("");

        // Attach initial format
        returnSmvFormat.append("MODULE main \n");
        returnSmvFormat.append("\tVAR \n");
        returnSmvFormat.append("\t\t" + this.getDisplayName() + ": "
                + this.getDisplayName() + "();\n");

        // Based on the user selection on formula type, add different
        // annotations: For CTL we use "SPEC"; for LTL we use "LTLSPEC".
        if (choice == FormulaType.CTL) {
            returnSmvFormat.append("\tSPEC \n");
            returnSmvFormat.append("\t\t" + formula + "\n");
        } else if (choice == FormulaType.LTL) {
            returnSmvFormat.append("\tLTLSPEC \n");
            returnSmvFormat.append("\t\t" + formula + "\n");
        }

        // Module definition
        returnSmvFormat.append("MODULE " + this.getDisplayName() + "() \n");
        returnSmvFormat.append("\tVAR \n");
        returnSmvFormat.append("\t\tstate : {");

        // Enumerate all states in the FmvAutomaton
        HashSet<State> frontier; // = new HashSet<State>();
        try {
            frontier = _enumerateStateSet();
        } catch (Exception exception) {
            throw new IllegalActionException(
                    "FmvAutomaton.convertToSMVFormat() clashes: "
                            + exception.getMessage());
        }

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

        // Decide variables encoded in the Kripke Structure
        HashSet<String> variableSet; // = new HashSet<String>();
        try {
            // Enumerate all variables used in the Kripke structure
            variableSet = _decideVariableSet(span);
        } catch (Exception exception) {
            throw new IllegalActionException(
                    "FmvAutomaton.convertToSMVFormat() clashes: "
                            + exception.getMessage());
        }

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
                        "FmvAutomaton.convertToSMVFormat() clashes:\nInternal error, getting \""
                                + valName
                                + "\" from \"_variableInfo\" returned null?");
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

        returnSmvFormat.append("\tASSIGN \n");

        // setup initial state
        try {
            String name = this.getInitialState().getName();
            returnSmvFormat.append("\t\tinit(state) := " + name + ";\n");
        } catch (Exception exception) {
            throw new IllegalActionException(
                    "FmvAutomaton.convertToSMVFormat() clashes: "
                            + exception.getMessage());
        }

        _generateAllVariableTransitions(variableSet);

        returnSmvFormat.append("\t\tnext(state) :=\n");
        returnSmvFormat.append("\t\t\tcase\n");

        // Generate all transitions; start from "state"
        LinkedList<VariableTransitionInfo> infoList = _variableTransitionInfo
                .get("state");
        if (infoList == null) {
            throw new IllegalActionException("Internal error, getting \""
                    + "state" + "\" returned null?");
        }
        for (int i = 0; i < infoList.size(); i++) {
            VariableTransitionInfo info = infoList.get(i);
            returnSmvFormat.append("\t\t\t\t" + info._preCondition + " :{ "
                    + info._variableNewValue + " };\n");
        }
        returnSmvFormat.append("\t\t\t\t1             : state;\n");
        returnSmvFormat.append("\t\t\tesac;\n\n");

        // Find out initial values for those variables.
        HashMap<String, String> variableInitialValue; // = new HashMap<String,

        variableInitialValue = _retrieveVariableInitialValue(variableSet);

        // Generate all transitions; run for every variable used in Kripke structure.
        Iterator<String> newItVariableSet = variableSet.iterator();
        while (newItVariableSet.hasNext()) {

            String valName = newItVariableSet.next();
            returnSmvFormat.append("\t\tinit(" + valName + ") := "
                    + variableInitialValue.get(valName) + ";\n");
            returnSmvFormat.append("\t\tnext(" + valName + ") :=\n");
            returnSmvFormat.append("\t\t\tcase\n");

            // Generate all transitions; start from "state"
            List<VariableTransitionInfo> innerInfoList = _variableTransitionInfo
                    .get(valName);
            if (innerInfoList == null) {
                throw new IllegalActionException(
                        "FmvAutomaton.convertToSMVFormat() clashes:\nInternal error, getting \""
                                + "state" + "\" returned null?");
            }
            for (int i = 0; i < innerInfoList.size(); i++) {
                VariableTransitionInfo info = innerInfoList.get(i);
                returnSmvFormat.append("\t\t\t\t" + info._preCondition + " :{ "
                        + info._variableNewValue + " };\n");

            }
            returnSmvFormat
            .append("\t\t\t\t1             : " + valName + ";\n");
            returnSmvFormat.append("\t\t\tesac;\n\n");
        }

        return returnSmvFormat;
    }

    /**
     * This private function first decides variables that would be used in the
     * Kripke structure. Once when it is decided, it performs step 1 and 2 of
     * the variable domain generation process.
     *
     * @param numSpan The size of the span used to expand the domain of a variable.
     * @return a set indicating the variable used in this automaton
     * @exception IllegalActionException
     */
    private HashSet<String> _decideVariableSet(int numSpan)
            throws IllegalActionException {

        HashSet<String> returnVariableSet = new HashSet<String>();
        HashSet<State> stateSet = new HashSet<State>();

        // initialize
        HashMap<String, State> frontier = new HashMap<String, State>();
        _variableInfo = new HashMap<String, VariableInfo>();

        // create initial state
        State stateInThis = this.getInitialState();
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
                throw new IllegalActionException(
                        "FmvAutomaton._decideVariableSet() clashes:\n Internal error, removing \""
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
                            String rValue = characters[1].trim();

                            if (Pattern.matches("^-?\\d+$",
                                    characters[1].trim()) == true) {

                                int numberRetrieval = Integer.parseInt(rValue);
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
                            "FmvAutomaton._decideVariableSet() clashes: "
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
     * @return A HashSet of states of a particular FmvAutomaton
     * @exception IllegalActionException
     */
    private HashSet<State> _enumerateStateSet() throws IllegalActionException {

        HashSet<State> returnStateSet = new HashSet<State>();
        try {
            // init
            HashMap<String, State> frontier = new HashMap<String, State>();

            // create initial state
            State stateInThis = this.getInitialState();
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
                if (name != null) {
                    stateInThis = frontier.remove(name);
                }
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
                    "FmvAutomaton._enumerateStateSet() clashes: "
                            + exception.getMessage());

        }
        return returnStateSet;
    }

    /**
     * Generate all premise-action pairs regarding this FmvAutomaton. For
     * example, this method may generate (state=red)&&(count=1):{grn}. This can
     * only be applied when the domain of variable is decided.
     */
    private void _generateAllVariableTransitions(HashSet<String> variableSet)
            throws IllegalActionException {

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
        State stateInThis = this.getInitialState();
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
                throw new IllegalActionException(
                        "FmvAutomaton._generateAllVariableTransitions clashes:\n"
                                + "Internal error, removing \"" + name
                                + "\" returned null?");
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

                // variableUsedInTransitionSet: Store variable names used in
                // this transition as preconditions. If in the guard
                // expression, we have X<3 && Y>5, then X and Y are used as
                // variables in precondition and should be stored in the
                // set "variableUsedInTransitionSet".

                // FIXME: (2008/01/22) Also, variables used in setAction should
                // be stored in the set "variableUsedInTransitionSet".
                HashSet<String> variableUsedInTransitionSet = new HashSet<String>();

                if (guard != null && !guard.trim().equals("")) {
                    if (hasAnnotation) {
                        // FIXME: (2007/12/14 Patrick.Cheng) Currently I
                        // don't know the meaning of annotation. Do nothing
                        // currently.
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
                                    // FIXME: (2007/12/14 Patrick.Cheng)
                                    // First case, synchronize usage.
                                    // Currently not implementing...
                                } else {
                                    // Store in the set. Use try-catch to
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
                                        variableUsedInTransitionSet.add(lValue);
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

                            //try {
                            variableUsedInTransitionSet.add(lValue);
                            //} catch (Exception ex) {

                            //}

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
                    // Retrieve the value in the
                    if (val != null) {
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
                                // Now perform the add up of new value: DOMAIN_GT and
                                // DOMAIN_LS into each of the
                                // variableDomainForTransition set. We make it a sorted
                                // list to facilitate further processing.
                                ArrayList<Integer> variableDomainForTransition = new ArrayList<Integer>();
                                variableDomainForTransition.add(DOMAIN_LS);
                                for (int number = lowerBound; number <= upperBound; number++) {
                                    // Place each possible value within boundary into
                                    // the list.
                                    variableDomainForTransition.add(Integer
                                            .valueOf(number));
                                }
                                variableDomainForTransition.add(DOMAIN_GT);
                                valueDomain.put(val,
                                        variableDomainForTransition);
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
                                    String lValue = characterOfSubGuard[0]
                                            .trim();
                                    boolean b = Pattern.matches(".*_isPresent",
                                            characterOfSubGuard[0].trim());
                                    if (b == true) {
                                        // FIXME: (2007/12/14 Patrick.Cheng)
                                        // First case, synchronize usage.
                                        // Currently not implementing...
                                    } else {
                                        // Check if the right value exists. We
                                        // need to ward off cases like "true".
                                        // This is achieved using try-catch and
                                        // retrieve the rValue from
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
                                                                .intValue() != numberRetrieval) {
                                                            domain.remove(j);
                                                        }
                                                    }
                                                    valueDomain.put(lValue,
                                                            domain);

                                                } else if (Pattern.matches(
                                                        ".*!=.*",
                                                        subGuardCondition)) {
                                                    // not equal
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
                                                                .intValue() == numberRetrieval) {
                                                            domain.remove(j);
                                                        }
                                                    }
                                                    valueDomain.put(lValue,
                                                            domain);

                                                } else if (Pattern.matches(
                                                        ".*<=.*",
                                                        subGuardCondition)) {
                                                    // less or equal than
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
                                                                .intValue() > numberRetrieval) {
                                                            domain.remove(j);
                                                        }
                                                    }
                                                    valueDomain.put(lValue,
                                                            domain);

                                                } else if (Pattern.matches(
                                                        ".*>=.*",
                                                        subGuardCondition)) {
                                                    // greater or equal than
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
                                                                .intValue() < numberRetrieval) {
                                                            domain.remove(j);
                                                        }
                                                    }
                                                    valueDomain.put(lValue,
                                                            domain);

                                                } else if (Pattern.matches(
                                                        ".*>.*",
                                                        subGuardCondition)) {
                                                    // greater than
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

                                                } else if (Pattern.matches(
                                                        ".*<.*",
                                                        subGuardCondition)) {
                                                    // less than
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

                    // setActions stores information about the update of the
                    // variable; outputActions stores information about the
                    // update of the variable that is going to be transmitted
                    // through the output port.

                    String setActionExpression = transition.setActions
                            .getExpression();

                    if (setActionExpression != null
                            && !setActionExpression.trim().equals("")) {
                        // Retrieve possible value of the variable
                        String[] splitExpression = setActionExpression
                                .split(";");
                        for (String element : splitExpression) {
                            String[] characters = element.split("=");
                            if (characters.length >= 2) {
                                String lValue = characters[0].trim();
                                String rValue = characters[1].trim();

                                if (Pattern.matches("^-?\\d+$",
                                        characters[1].trim()) == true) {

                                    // Generate all possible conditions that leads
                                    // to this change.

                                    String statePrecondition = "state="
                                            + stateInThis.getDisplayName();
                                    _generatePremiseAndResultEachTransition(
                                            statePrecondition, valueDomain,
                                            lValue, rValue, "N");
                                    _generatePremiseAndResultEachTransition(
                                            statePrecondition, valueDomain,
                                            "state",
                                            destinationInThis.getDisplayName(),
                                            "S");

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

                                    if (Pattern.matches(".*[*].*", rValue)) {

                                        String[] rValueOperends = rValue
                                                .split("[*]");

                                        String offset = rValueOperends[1]
                                                .trim();

                                        try {
                                            /*int value =*/Integer
                                            .parseInt(rValueOperends[1]
                                                    .trim());
                                        } catch (Exception ex) {
                                            // check if the value is of format
                                            // (-a)
                                            if (rValueOperends[1].trim()
                                                    .endsWith(")")
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
                                                            "FmvAutomaton._generateAllVariableTransitions() clashes:\n"
                                                                    + "format not supported by the conversion process.");
                                                }

                                            }

                                        }
                                        // set up all possible transitions
                                        // regarding to this assignment.

                                        String statePrecondition = "state="
                                                + stateInThis.getDisplayName();

                                        _generatePremiseAndResultEachTransition(
                                                statePrecondition, valueDomain,
                                                lValue, offset, "*");
                                        _generatePremiseAndResultEachTransition(
                                                statePrecondition, valueDomain,
                                                "state",
                                                destinationInThis
                                                .getDisplayName(), "S");

                                    } else if (Pattern.matches(".*/.*", rValue)) {

                                        String[] rValueOperends = rValue
                                                .split("[/]");

                                        String offset = rValueOperends[1]
                                                .trim();

                                        try {
                                            Integer.parseInt(rValueOperends[1]
                                                    .trim());
                                        } catch (Exception ex) {
                                            // check if the value is of format
                                            // (-a)
                                            if (rValueOperends[1].trim()
                                                    .endsWith(")")
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
                                                }

                                            }

                                        }
                                        // set up all possible transitions
                                        // regarding to this assignment.

                                        String statePrecondition = "state="
                                                + stateInThis.getDisplayName();

                                        _generatePremiseAndResultEachTransition(
                                                statePrecondition, valueDomain,
                                                lValue, offset, "/");
                                        _generatePremiseAndResultEachTransition(
                                                statePrecondition, valueDomain,
                                                "state",
                                                destinationInThis
                                                .getDisplayName(), "S");

                                    } else if (Pattern.matches(".*+.*", rValue)) {

                                        String[] rValueOperends = rValue
                                                .split("[+]");

                                        String offset = rValueOperends[1]
                                                .trim();

                                        try {
                                            Integer.parseInt(rValueOperends[1]
                                                    .trim());
                                        } catch (Exception ex) {
                                            // check if the value is of format
                                            // (-a)
                                            if (rValueOperends[1].trim()
                                                    .endsWith(")")
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
                                                    System.err
                                                            .println("FmvAutomaton: failed to parse \""
                                                                    + offset
                                                                    + "\": "
                                                                    + exInner);
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
                                                statePrecondition, valueDomain,
                                                lValue,
                                                rValueOperends[1].trim(), "+");

                                        _generatePremiseAndResultEachTransition(
                                                statePrecondition, valueDomain,
                                                "state",
                                                destinationInThis
                                                .getDisplayName(), "S");

                                    } else if (Pattern.matches(".*-.*", rValue)) {

                                        String[] rValueOperends = rValue
                                                .split("[-]");

                                        String offset = rValueOperends[1]
                                                .trim();

                                        try {
                                            Integer.parseInt(rValueOperends[1]
                                                    .trim());
                                        } catch (Exception ex) {
                                            // check if the value is of format
                                            // (-a)
                                            if (rValueOperends[1].trim()
                                                    .endsWith(")")
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
                                                    System.err
                                                            .println("FmvAutomaton: failed to parse \""
                                                                    + offset
                                                                    + "\": "
                                                                    + exInner);
                                                }
                                            }
                                        }
                                        // set up all possible transitions
                                        // regarding to this assignment.

                                        String statePrecondition = "state="
                                                + stateInThis.getDisplayName();

                                        _generatePremiseAndResultEachTransition(
                                                statePrecondition, valueDomain,
                                                lValue,
                                                rValueOperends[1].trim(), "-");
                                        _generatePremiseAndResultEachTransition(
                                                statePrecondition, valueDomain,
                                                "state",
                                                destinationInThis
                                                .getDisplayName(), "S");

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
                                statePrecondition, valueDomain, "state",
                                destinationInThis.getDisplayName(), "S");
                    }
                }
            }

        }

    }

    /**
     * This function is used to generate detailed pre-conditions and
     * post-conditions in .smv format. It is used by the function
     * _generateAllVariableTransitions()
     */
    private void _generatePremiseAndResultEachTransition(
            String statePrecondition,
            HashMap<String, ArrayList<Integer>> valueDomain, String lValue,
            String offset, String operatingSign) throws IllegalActionException {

        // 1. If operatingSign=="N", then offset means the value that needs to
        // be assigned.
        // 2. if operatingSign=="S", then offset means the destination vertex
        // label.
        // 3. For rest cases (operatingSign=="+","-","*","/"), variable
        // has "X = X operatingSign offset".

        String[] keySetArray = valueDomain.keySet().toArray(
                new String[valueDomain.keySet().size()]);

        _recursiveStepGeneratePremiseAndResultEachTransition(statePrecondition,
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
    private void _recursiveStepGeneratePremiseAndResultEachTransition(
            String currentPremise, int index, int maxIndex,
            String[] keySetArray,
            HashMap<String, ArrayList<Integer>> valueDomain, String lValue,
            String newVariableValue, String operatingSign)
                    throws IllegalActionException {

        if (lValue != null) {
            if (index >= maxIndex) {
                // Store in the array

                VariableTransitionInfo newTransitionInfo = new VariableTransitionInfo();
                newTransitionInfo._preCondition = currentPremise;
                newTransitionInfo._variableNewValue = newVariableValue;
                LinkedList<VariableTransitionInfo> temp = _variableTransitionInfo
                        .remove(lValue);
                if (temp == null) {
                    throw new IllegalActionException(
                            "Internal error, removing \"" + lValue
                            + "\" returned null?");
                }
                temp.add(newTransitionInfo);
                _variableTransitionInfo.put(lValue, temp);

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
                                        String newPremise = currentPremise
                                                + " & " + keySetArray[index]
                                                        + "=" + "gt";

                                        // When the original value is GT, then
                                        // GT + positive_const = GT
                                        // Hence the updated value remains the same.
                                        _recursiveStepGeneratePremiseAndResultEachTransition(
                                                newPremise, index + 1,
                                                maxIndex, keySetArray,
                                                valueDomain, lValue, "gt",
                                                operatingSign);

                                    } else if (vList.get(i).intValue() == DOMAIN_LS) {
                                        // For DOMAIN_LS, we place conservative
                                        // analysis and assert that it might lead to
                                        // all its possible values. For example, if
                                        // min=1, and offset=3, then possible value
                                        // may include LS, 1, 2.

                                        String newPremise = currentPremise
                                                + " & " + keySetArray[index]
                                                        + "=" + "ls";
                                        // String newPremise = new String(
                                        // currentPremise + " & "
                                        // + keySetArray[index] + "="
                                        // + "ls");
                                        // First, LS + positive_const = LS
                                        _recursiveStepGeneratePremiseAndResultEachTransition(
                                                newPremise, index + 1,
                                                maxIndex, keySetArray,
                                                valueDomain, lValue, "ls",
                                                operatingSign);
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
                                                    // never exceeds upper bound. If it
                                                    // is below lower bound, we must
                                                    // stop it
                                                    // and use GT to replace the value.

                                                    if (minimumInBoundary + j > Integer
                                                            .parseInt(variableInfo._maxValue)) {
                                                        _recursiveStepGeneratePremiseAndResultEachTransition(
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
                                                    _recursiveStepGeneratePremiseAndResultEachTransition(
                                                            newPremise,
                                                            index + 1,
                                                            maxIndex,
                                                            keySetArray,
                                                            valueDomain,
                                                            lValue,
                                                            updatedVariableValue,
                                                            operatingSign);
                                                }
                                            }
                                        }

                                    } else {
                                        // For ordinary cases, we only need to check
                                        // if the new value would exceeds the upper
                                        // bound. If so, then use DOMAIN_GT to
                                        // replace the value.

                                        String newPremise = currentPremise
                                                + " & "
                                                + keySetArray[index]
                                                        + "="
                                                        + String.valueOf(vList.get(i)
                                                                .intValue());

                                        String updatedVariableValue = String
                                                .valueOf(vList.get(i)
                                                        .intValue()
                                                        + Integer
                                                        .parseInt(newVariableValue));
                                        VariableInfo variableInfo = _variableInfo
                                                .get(lValue);
                                        if (variableInfo != null) {
                                            if (variableInfo._maxValue != null) {
                                                if (vList.get(i).intValue()
                                                        + Integer
                                                        .parseInt(newVariableValue) > Integer
                                                        .parseInt(variableInfo._maxValue)) {
                                                    // Use DOMAIN_GT to replace the value.
                                                    updatedVariableValue = "gt";
                                                }
                                            }
                                        }

                                        _recursiveStepGeneratePremiseAndResultEachTransition(
                                                newPremise, index + 1,
                                                maxIndex, keySetArray,
                                                valueDomain, lValue,
                                                updatedVariableValue,
                                                operatingSign);
                                    }
                                } else {
                                    // Offset negative case (negative_const)

                                    if (vList.get(i).intValue() == DOMAIN_LS) {

                                        String newPremise = currentPremise
                                                + " & " + keySetArray[index]
                                                        + "=" + "ls";

                                        // When the original value is LS, then
                                        // LS + negative_const = LS
                                        // Hence the updated value remains the same.
                                        _recursiveStepGeneratePremiseAndResultEachTransition(
                                                newPremise, index + 1,
                                                maxIndex, keySetArray,
                                                valueDomain, lValue, "ls",
                                                operatingSign);

                                    } else if (vList.get(i).intValue() == DOMAIN_GT) {
                                        String newPremise = currentPremise
                                                + " & " + keySetArray[index]
                                                        + "=" + "gt";

                                        // When the original value is GT, we place
                                        // conservative analysis and assert that it
                                        // might lead to all its possible values.

                                        // First case: GT + negative_const = GT
                                        _recursiveStepGeneratePremiseAndResultEachTransition(
                                                newPremise, index + 1,
                                                maxIndex, keySetArray,
                                                valueDomain, lValue, "gt",
                                                operatingSign);
                                        VariableInfo variableInfo = _variableInfo
                                                .get(lValue);
                                        if (variableInfo == null) {
                                            throw new IllegalActionException(
                                                    "Internal error, getting \""
                                                            + lValue
                                                            + "\" returned null?");
                                        } else {
                                            if (variableInfo._maxValue != null
                                                    && variableInfo._minValue != null) {
                                                int maximumInBoundary = Integer
                                                        .parseInt(variableInfo._maxValue);
                                                for (int j = 0; j > Integer
                                                        .parseInt(newVariableValue); j--) {
                                                    // here j-- because newVariableValue
                                                    // is
                                                    // negative

                                                    // We need to make sure that it
                                                    // would
                                                    // never exceeds upper bound. If it
                                                    // is below lower bound, we must
                                                    // stop it
                                                    // and use LS to replace the value.

                                                    if (variableInfo._minValue != null) {
                                                        if (maximumInBoundary
                                                                + j < Integer
                                                                .parseInt(variableInfo._minValue)) {
                                                            _recursiveStepGeneratePremiseAndResultEachTransition(
                                                                    newPremise,
                                                                    index + 1,
                                                                    maxIndex,
                                                                    keySetArray,
                                                                    valueDomain,
                                                                    lValue,
                                                                    "ls",
                                                                    operatingSign);
                                                            break;
                                                        }

                                                        String updatedVariableValue = String
                                                                .valueOf(maximumInBoundary
                                                                        + j);
                                                        _recursiveStepGeneratePremiseAndResultEachTransition(
                                                                newPremise,
                                                                index + 1,
                                                                maxIndex,
                                                                keySetArray,
                                                                valueDomain,
                                                                lValue,
                                                                updatedVariableValue,
                                                                operatingSign);
                                                    }

                                                }
                                            }

                                        }
                                    } else {
                                        // For ordinary cases, we only need to check
                                        // if the new value would exceeds the lower
                                        // bound. If so, then use DOMAIN_LS to
                                        // replace the value.

                                        String newPremise = currentPremise
                                                + " & "
                                                + keySetArray[index]
                                                        + "="
                                                        + String.valueOf(vList.get(i)
                                                                .intValue());

                                        String updatedVariableValue = String
                                                .valueOf(vList.get(i)
                                                        .intValue()
                                                        + Integer
                                                        .parseInt(newVariableValue));
                                        VariableInfo variableInfo = _variableInfo
                                                .get(lValue);
                                        if (variableInfo != null) {
                                            if (variableInfo._minValue != null) {
                                                if (vList.get(i).intValue()
                                                        + Integer
                                                        .parseInt(newVariableValue) < Integer
                                                        .parseInt(variableInfo._minValue)) {
                                                    // Use DOMAIN_LS to replace the value.
                                                    updatedVariableValue = "ls";
                                                }

                                                _recursiveStepGeneratePremiseAndResultEachTransition(
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

                            _recursiveStepGeneratePremiseAndResultEachTransition(
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

                                        String newPremise = currentPremise
                                                + " & " + keySetArray[index]
                                                        + "=" + "ls";

                                        // Hence the updated value remains the same.
                                        _recursiveStepGeneratePremiseAndResultEachTransition(
                                                newPremise, index + 1,
                                                maxIndex, keySetArray,
                                                valueDomain, lValue, "ls",
                                                operatingSign);

                                    } else if (vList.get(i).intValue() == DOMAIN_GT) {
                                        // If original variable value is GT, we
                                        // place conservative analysis and assert
                                        // that it might lead to all its possible
                                        // values.

                                        String newPremise = currentPremise
                                                + " & " + keySetArray[index]
                                                        + "=" + "gt";

                                        // First, it may keep to be GT
                                        _recursiveStepGeneratePremiseAndResultEachTransition(
                                                newPremise, index + 1,
                                                maxIndex, keySetArray,
                                                valueDomain, lValue, "gt",
                                                operatingSign);

                                        VariableInfo variableInfo = _variableInfo
                                                .get(lValue);
                                        if (variableInfo != null) {
                                            if (variableInfo._minValue != null
                                                    && variableInfo._maxValue != null) {
                                                int maximumInBoundary = Integer
                                                        .parseInt(variableInfo._maxValue);
                                                for (int j = 0; j < Integer
                                                        .parseInt(newVariableValue); j++) {

                                                    // We need to make sure that it would
                                                    // never exceeds upper bound. If it
                                                    // is below lower bound, we must stop it
                                                    // and use LS to replace the value.

                                                    if (maximumInBoundary - j < Integer
                                                            .parseInt(variableInfo._minValue)) {
                                                        _recursiveStepGeneratePremiseAndResultEachTransition(
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
                                                    _recursiveStepGeneratePremiseAndResultEachTransition(
                                                            newPremise,
                                                            index + 1,
                                                            maxIndex,
                                                            keySetArray,
                                                            valueDomain,
                                                            lValue,
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
                                                .valueOf(vList.get(i)
                                                        .intValue()
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
                                                    // Use DOMAIN_LS to replace the value.
                                                    updatedVariableValue = "ls";
                                                }

                                                _recursiveStepGeneratePremiseAndResultEachTransition(
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

                                    if (vList.get(i).intValue() == DOMAIN_GT) {
                                        // GT - negative_const = GT

                                        String newPremise = currentPremise
                                                + " & " + keySetArray[index]
                                                        + "=" + "gt";

                                        // Hence the updated value remains the same.
                                        _recursiveStepGeneratePremiseAndResultEachTransition(
                                                newPremise, index + 1,
                                                maxIndex, keySetArray,
                                                valueDomain, lValue, "gt",
                                                operatingSign);

                                    } else if (vList.get(i).intValue() == DOMAIN_LS) {
                                        // For DOMAIN_LS, we place conservative
                                        // analysis and assert that it might lead to
                                        // all its possible values

                                        String newPremise = currentPremise
                                                + " & " + keySetArray[index]
                                                        + "=" + "ls";

                                        // First, LS - negative_const = LS
                                        _recursiveStepGeneratePremiseAndResultEachTransition(
                                                newPremise, index + 1,
                                                maxIndex, keySetArray,
                                                valueDomain, lValue, "ls",
                                                operatingSign);
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
                                                    // never exceeds upper bound. If it
                                                    // exceeds upper bound, we must stop
                                                    // it
                                                    // and use GT to replace the value.

                                                    if (minimumInBoundary - j < Integer
                                                            .parseInt(variableInfo._maxValue)) {
                                                        _recursiveStepGeneratePremiseAndResultEachTransition(
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

                                                    _recursiveStepGeneratePremiseAndResultEachTransition(
                                                            newPremise,
                                                            index + 1,
                                                            maxIndex,
                                                            keySetArray,
                                                            valueDomain,
                                                            lValue,
                                                            updatedVariableValue,
                                                            operatingSign);

                                                }
                                            }
                                        }
                                    } else {
                                        // For ordinary part, we only need to check
                                        // if the new value would exceeds the upper
                                        // bound. If so, then use DOMAIN_GT to
                                        // replace the value.

                                        String newPremise = currentPremise
                                                + " & "
                                                + keySetArray[index]
                                                        + "="
                                                        + String.valueOf(vList.get(i)
                                                                .intValue());

                                        String updatedVariableValue = String
                                                .valueOf(vList.get(i)
                                                        .intValue()
                                                        - Integer
                                                        .parseInt(newVariableValue));
                                        VariableInfo variableInfo = _variableInfo
                                                .get(lValue);
                                        if (variableInfo != null) {
                                            if (variableInfo._maxValue != null) {
                                                if (vList.get(i).intValue()
                                                        - Integer
                                                        .parseInt(newVariableValue) > Integer
                                                        .parseInt(variableInfo._maxValue)) {
                                                    // Use DOMAIN_LS to replace the value.
                                                    updatedVariableValue = "gt";
                                                }
                                            }
                                        }

                                        _recursiveStepGeneratePremiseAndResultEachTransition(
                                                newPremise, index + 1,
                                                maxIndex, keySetArray,
                                                valueDomain, lValue,
                                                updatedVariableValue,
                                                operatingSign);
                                    }
                                }
                            }

                        } else {
                            _recursiveStepGeneratePremiseAndResultEachTransition(
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
                                        String newPremise = currentPremise
                                                + " & " + keySetArray[index]
                                                        + "=" + "gt";
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
                                                    // when max>=0, GT * positive_const
                                                    // = GT
                                                    // Hence the updated value remains
                                                    // the
                                                    // same.
                                                    _recursiveStepGeneratePremiseAndResultEachTransition(
                                                            newPremise,
                                                            index + 1,
                                                            maxIndex,
                                                            keySetArray,
                                                            valueDomain,
                                                            lValue, "gt",
                                                            operatingSign);
                                                } else {
                                                    // Integer.parseInt(((VariableInfo)
                                                    // _variableInfo.get(lValue))._maxValue)
                                                    // < 0
                                                    //
                                                    // Starting from the upper bound +
                                                    // 1,
                                                    // +2, +3, +4 ... calculate all
                                                    // possible
                                                    // values until the new set-value is
                                                    // greater than GT.
                                                    //
                                                    // For example, if upper bound is
                                                    // -5,
                                                    // and if the offset is 2, then for
                                                    // values in GT that is greater or
                                                    // equal
                                                    // to -2, the new variable would be
                                                    // in
                                                    // GT. But if the lower bound is -7,
                                                    // then we need to replace cases
                                                    // that is
                                                    // lower to -7. For example,
                                                    // -4*2=-8. We
                                                    // should use LS to represent this
                                                    // value.
                                                    //
                                                    // Also we expect to record one LS
                                                    // as the new value only. So there
                                                    // are tricks that needs to be
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
                                                            // tricks mentioned above.
                                                            _recursiveStepGeneratePremiseAndResultEachTransition(
                                                                    newPremise,
                                                                    index + 1,
                                                                    maxIndex,
                                                                    keySetArray,
                                                                    valueDomain,
                                                                    lValue,
                                                                    "ls",
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
                                                            _recursiveStepGeneratePremiseAndResultEachTransition(
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

                                                    _recursiveStepGeneratePremiseAndResultEachTransition(
                                                            newPremise,
                                                            index + 1,
                                                            maxIndex,
                                                            keySetArray,
                                                            valueDomain,
                                                            lValue, "gt",
                                                            operatingSign);

                                                }
                                            }
                                        }
                                    } else if (vList.get(i).intValue() == DOMAIN_LS) {

                                        String newPremise = currentPremise
                                                + " & " + keySetArray[index]
                                                        + "=" + "ls";

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
                                                    // when min<=0, LS * positive_const
                                                    // = LS
                                                    // Hence the updated value remains
                                                    // the
                                                    // same.
                                                    _recursiveStepGeneratePremiseAndResultEachTransition(
                                                            newPremise,
                                                            index + 1,
                                                            maxIndex,
                                                            keySetArray,
                                                            valueDomain,
                                                            lValue, "ls",
                                                            operatingSign);
                                                } else {
                                                    // Starting from the lower bound -1,
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

                                                            _recursiveStepGeneratePremiseAndResultEachTransition(
                                                                    newPremise,
                                                                    index + 1,
                                                                    maxIndex,
                                                                    keySetArray,
                                                                    valueDomain,
                                                                    lValue,
                                                                    "gt",
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
                                                            _recursiveStepGeneratePremiseAndResultEachTransition(
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
                                                    _recursiveStepGeneratePremiseAndResultEachTransition(
                                                            newPremise,
                                                            index + 1,
                                                            maxIndex,
                                                            keySetArray,
                                                            valueDomain,
                                                            lValue, "ls",
                                                            operatingSign);

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
                                                .valueOf(vList.get(i)
                                                        .intValue()
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

                                                } else if (vList.get(i)
                                                        .intValue()
                                                        * Integer
                                                        .parseInt(newVariableValue) > Integer
                                                        .parseInt(variableInfo._maxValue)) {
                                                    updatedVariableValue = "gt";
                                                }

                                                _recursiveStepGeneratePremiseAndResultEachTransition(
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

                                        String newPremise = currentPremise
                                                + " & " + keySetArray[index]
                                                        + "=" + "gt";
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
                                                    // +1, +2, +3, +4 ...
                                                    // calculate all possible values
                                                    // until the value is less than LS.
                                                    //
                                                    // For example, if upper bound = 1,
                                                    // lower bound = -7, and offset =
                                                    // -2,
                                                    // then we might have possible new
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
                                                        _recursiveStepGeneratePremiseAndResultEachTransition(
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

                                                    _recursiveStepGeneratePremiseAndResultEachTransition(
                                                            newPremise,
                                                            index + 1,
                                                            maxIndex,
                                                            keySetArray,
                                                            valueDomain,
                                                            lValue, "ls",
                                                            operatingSign);

                                                } else if (Integer
                                                        .parseInt(variableInfo._maxValue) < 0) {
                                                    // One important thing is that we
                                                    // may
                                                    // have cases where 0 * const = 0.
                                                    // Because 0 is in GT, so we would
                                                    // have
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

                                                            _recursiveStepGeneratePremiseAndResultEachTransition(
                                                                    newPremise,
                                                                    index + 1,
                                                                    maxIndex,
                                                                    keySetArray,
                                                                    valueDomain,
                                                                    lValue,
                                                                    "gt",
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
                                                            _recursiveStepGeneratePremiseAndResultEachTransition(
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
                                                    _recursiveStepGeneratePremiseAndResultEachTransition(
                                                            newPremise,
                                                            index + 1,
                                                            maxIndex,
                                                            keySetArray,
                                                            valueDomain,
                                                            lValue, "ls",
                                                            operatingSign);

                                                    // Special case where 0 * const = 0
                                                    _recursiveStepGeneratePremiseAndResultEachTransition(
                                                            newPremise,
                                                            index + 1,
                                                            maxIndex,
                                                            keySetArray,
                                                            valueDomain,
                                                            lValue, "gt",
                                                            operatingSign);

                                                }
                                            }
                                        }
                                    } else if (vList.get(i).intValue() == DOMAIN_LS) {
                                        // (Integer.parseInt(newVariableValue) < 0)
                                        // && original variable value == DOMAIN_LS

                                        String newPremise = currentPremise
                                                + " & " + keySetArray[index]
                                                        + "=" + "ls";
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
                                                    // Starting from the lower bound -1,
                                                    // -2, -3, -4 ...
                                                    // calculate all possible values
                                                    // until
                                                    // the value is less than GT.
                                                    //
                                                    // For example, if upper bound = 7,
                                                    // lower bound = -1, and offset =
                                                    // -2,
                                                    // then we might have possible new
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
                                                        _recursiveStepGeneratePremiseAndResultEachTransition(
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

                                                    _recursiveStepGeneratePremiseAndResultEachTransition(
                                                            newPremise,
                                                            index + 1,
                                                            maxIndex,
                                                            keySetArray,
                                                            valueDomain,
                                                            lValue, "gt",
                                                            operatingSign);

                                                } else if (Integer
                                                        .parseInt(variableInfo._minValue) > 0) {
                                                    // One important thing is that we
                                                    // may
                                                    // have cases where 0 * const = 0.
                                                    // Because 0 is in LS, so we would
                                                    // have
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

                                                            _recursiveStepGeneratePremiseAndResultEachTransition(
                                                                    newPremise,
                                                                    index + 1,
                                                                    maxIndex,
                                                                    keySetArray,
                                                                    valueDomain,
                                                                    lValue,
                                                                    "ls",
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
                                                            _recursiveStepGeneratePremiseAndResultEachTransition(
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
                                                    _recursiveStepGeneratePremiseAndResultEachTransition(
                                                            newPremise,
                                                            index + 1,
                                                            maxIndex,
                                                            keySetArray,
                                                            valueDomain,
                                                            lValue, "gt",
                                                            operatingSign);

                                                    // Special case where 0 * const = 0
                                                    _recursiveStepGeneratePremiseAndResultEachTransition(
                                                            newPremise,
                                                            index + 1,
                                                            maxIndex,
                                                            keySetArray,
                                                            valueDomain,
                                                            lValue, "ls",
                                                            operatingSign);

                                                }
                                            }
                                        }
                                    } else {
                                        // For ordinary part, we only need to check
                                        // if the new value would exceeds the upper
                                        // bound. If so, then use DOMAIN_GT to
                                        // replace the value.

                                        String newPremise = currentPremise
                                                + " & "
                                                + keySetArray[index]
                                                        + "="
                                                        + String.valueOf(vList.get(i)
                                                                .intValue());

                                        String updatedVariableValue = String
                                                .valueOf(vList.get(i)
                                                        .intValue()
                                                        - Integer
                                                        .parseInt(newVariableValue));
                                        VariableInfo variableInfo = _variableInfo
                                                .get(lValue);
                                        if (variableInfo != null) {
                                            if (variableInfo._maxValue != null) {
                                                if (vList.get(i).intValue()
                                                        - Integer
                                                        .parseInt(newVariableValue) > Integer
                                                        .parseInt(variableInfo._maxValue)) {
                                                    // Use DOMAIN_LS to replace the value.
                                                    updatedVariableValue = "gt";
                                                }
                                            }
                                        }

                                        _recursiveStepGeneratePremiseAndResultEachTransition(
                                                newPremise, index + 1,
                                                maxIndex, keySetArray,
                                                valueDomain, lValue,
                                                updatedVariableValue,
                                                operatingSign);
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
                                                + keySetArray[index] + "="
                                                + "ls";
                                    } else if (vList.get(i).intValue() == DOMAIN_GT) {
                                        newPremise = currentPremise + " & "
                                                + keySetArray[index] + "="
                                                + "gt";
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
                                                // Use DOMAIN_LS to replace the value.
                                                updatedVariableValue = "gt";
                                            } else if (0 < Integer
                                                    .parseInt(variableInfo._minValue)) {
                                                updatedVariableValue = "ls";
                                            }

                                            _recursiveStepGeneratePremiseAndResultEachTransition(
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
                            _recursiveStepGeneratePremiseAndResultEachTransition(
                                    currentPremise, index + 1, maxIndex,
                                    keySetArray, valueDomain, lValue,
                                    newVariableValue, operatingSign);
                        }

                    } else if (operatingSign.equalsIgnoreCase("/")) {
                        // FIXME: Right now the execution of division is not
                        // implemented.

                        ArrayList<Integer> vList = valueDomain
                                .get(keySetArray[index]);
                        if (vList != null && vList.size() != 0) {
                            for (int i = 0; i < vList.size(); i++) {
                                String updatedVariableValue = String
                                        .valueOf(vList.get(i).intValue()
                                                / Integer
                                                .parseInt(newVariableValue));
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

                                _recursiveStepGeneratePremiseAndResultEachTransition(
                                        newPremise, index + 1, maxIndex,
                                        keySetArray, valueDomain, lValue,
                                        updatedVariableValue, operatingSign);
                            }
                        } else {
                            _recursiveStepGeneratePremiseAndResultEachTransition(
                                    currentPremise, index + 1, maxIndex,
                                    keySetArray, valueDomain, lValue,
                                    newVariableValue, operatingSign);
                        }

                    } else if (operatingSign.equalsIgnoreCase("N")) {

                        ArrayList<Integer> vList = valueDomain
                                .get(keySetArray[index]);
                        if (vList != null && vList.size() != 0) {
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
                                            + keySetArray[index] + "=" + "ls";
                                } else if (vList.get(i).intValue() == DOMAIN_GT) {
                                    newPremise = currentPremise + " & "
                                            + keySetArray[index] + "=" + "gt";
                                }
                                _recursiveStepGeneratePremiseAndResultEachTransition(
                                        newPremise, index + 1, maxIndex,
                                        keySetArray, valueDomain, lValue,
                                        updatedVariableValue, operatingSign);
                            }
                        } else {
                            _recursiveStepGeneratePremiseAndResultEachTransition(
                                    currentPremise, index + 1, maxIndex,
                                    keySetArray, valueDomain, lValue,
                                    newVariableValue, operatingSign);
                        }

                    }

                } else {
                    // meaning: if
                    // (keySetArray[index].equalsIgnoreCase(lValue)==false)
                    ArrayList<Integer> vList = valueDomain
                            .get(keySetArray[index]);
                    if (vList != null && vList.size() != 0) {
                        for (int i = 0; i < vList.size(); i++) {

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
                            _recursiveStepGeneratePremiseAndResultEachTransition(
                                    newPremise, index + 1, maxIndex,
                                    keySetArray, valueDomain, lValue,
                                    newVariableValue, operatingSign);
                        }
                    } else {
                        _recursiveStepGeneratePremiseAndResultEachTransition(
                                currentPremise, index + 1, maxIndex,
                                keySetArray, valueDomain, lValue,
                                newVariableValue, operatingSign);
                    }
                }

            }
        }

    }

    /**
     * A private function used as to generate variable initial values for the
     * initial variable set. This is achieved using a scan on all transitions in
     * edges (equalities/ inequalities) and retrieve all integer values in the
     * system. Currently the span is not taken into consideration.
     *
     * @param variableSet
     *                Set of variables that expect to find initial values.
     *
     */
    private HashMap<String, String> _retrieveVariableInitialValue(
            HashSet<String> variableSet) {

        // One problem regarding the initial value retrieval from parameters
        // is that when retrieving parameters, the return value would consist
        // of some undesirable infomation. We need to use split to do further
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
                propertyList = this.getAttribute(attribute).description()
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

    private HashMap<String, VariableInfo> _variableInfo;
    private HashMap<String, LinkedList<VariableTransitionInfo>> _variableTransitionInfo;

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

        /** Record set of conditions that leads to the change of variable
         *  _variableName.
         */
        private String _variableNewValue = null;
    }
}
