/* This director extends FSMDirector by consuming only input tokens
   that are needed in the current state.

   Copyright (c) 2004-2005 The Regents of the University of California.
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
package ptolemy.domains.fsm.kernel;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedActor;
import ptolemy.actor.util.DFUtilities;
import ptolemy.data.expr.ASTPtAssignmentNode;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.ParseTreeFreeVariableCollector;
import ptolemy.data.expr.ParserScope;
import ptolemy.data.expr.PtParser;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;


//////////////////////////////////////////////////////////////////////////
//// NonStrictFSMDirector

/**
   This director extends FSMDirector by consuming only input tokens that
   are needed in the current state. An input port will consume at most one
   token if:
   <p>
   1. The port is referred by any guard expression of the preemptive
   transitions leaving the current state, the output actions
   and/or set actions of the enabled transition.
   <p>
   2. No preemptive transition is enabled and the port is referred by
   the refinements of the current state, any guard expression of the
   nonpreemptive transitions leaving the current state, the output
   actions and/or set actions of the enabled transition.
   <p>
   A port is said to be referred by a guard/output action/set action
   expression of a transition if the port name appears in that expression.
   A port is said to be referred by a state refinement if the it is
   not a dangling port and has a consumption rate greater than zero in
   the refinement.
   <p>
   FIXME: This is highly preliminary. Missing capabilities:
   FIXME: Currently this director uses the default receiver of FSMDirector,
   which is a mailbox, so there is no way to consume more than one token.
   This director could use a different receiver and support a syntax in
   the guard expression language to support consumption of more than one
   token.

   @author Ye Zhou
   @version $Id$
   @since Ptolemy II 4.1
   @Pt.ProposedRating Red (zhouye)
   @Pt.AcceptedRating Red (cxh)
*/
public class NonStrictFSMDirector extends FSMDirector {
    /** Construct a director in the given container with the given name.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *  @param container Container of this director.
     *  @param name Name of this director.
     *  @exception IllegalActionException If the name has a period in it, or
     *   the director is not compatible with the specified container.
     *  @exception NameDuplicationException If the container not a
     *   CompositeActor and the name collides with an entity in the container.
     */
    public NonStrictFSMDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Fire the modal model. The preemptive transitions from the current
     *  state are examined. If there is more than one transition enabled,
     *  an exception is thrown. If there is exactly one preemptive transition
     *  enabled, then it is chosen. Get additional input ports referred by
     *  the output actions and set actions of the enabled transition and
     *  executed the output actions. The refinements of the current state will
     *  not be fired.
     *  <p>
     *  If no preemptive transition is enabled, get additional input ports
     *  referred by the refinements of the current state and transfer at most
     *  one token from these input ports. Fire the refinements. After this,
     *  get additional input ports referred by the nonpreemptive transitions
     *  from the current state and transfer at most one token from these input
     *  ports. Then examine the nonpreemptive transitions. If there is more
     *  than one transition enabled, an exception is thrown. If there is
     *  exactly one nonpreemptive transition enabled, then it is chosen. Get
     *  additional input ports referred by the output actions and set actions
     *  of the enabled transition and executed the output actions.
     *  @exception IllegalActionException If the super class throws it.
     */
    public void fire() throws IllegalActionException {
        FSMActor controller = getController();
        controller._readInputs();

        CompositeActor container = (CompositeActor) getContainer();
        List inputPortList = container.inputPortList();
        State currentState = controller.currentState();

        // Choose a nonpreemptive transition.
        List enabledTransitions = controller._checkTransition(currentState
                .preemptiveTransitionList());

        // Ensure that if there are multiple enabled transitions, all of them
        // must be nondeterministic.
        if (enabledTransitions.size() > 1) {
            Iterator transitions = enabledTransitions.iterator();

            while (transitions.hasNext()) {
                Transition enabledTransition = (Transition) transitions.next();

                if (!enabledTransition.isNondeterministic()) {
                    throw new MultipleEnabledTransitionsException(controller
                            .currentState(),
                            "Multiple enabled transitions found but "
                            + enabledTransition.getName() + " is deterministic.");
                }
            }
        }

        Transition enabledTransition = null;

        // Randomly choose one transition from the list of the
        // enabled trnasitions.
        int length = enabledTransitions.size();

        if (length != 0) {
            // Since the size of the list of enabled transitions usually (almost
            // always) is less than the maximum value of integer. We can safely
            // do the cast from long to int in the following statement.
            int randomChoice = (int) Math.floor(Math.random() * length);

            // There is tiny chance that randomChoice equals length.
            // When this happens, we deduct 1 from the randomChoice.
            if (randomChoice == length) {
                randomChoice--;
            }

            enabledTransition = (Transition) enabledTransitions.get(randomChoice);
        }

        _enabledTransition = enabledTransition;

        if (enabledTransition == null) {
            // Get the inputs needed by the refinement.
            Actor[] actors = currentState.getRefinement();
            getRefinementReferredInputPorts(currentState);

            // Transfer additional inputs needed by the refinement.
            for (int i = 0; i < inputPortList.size(); i++) {
                IOPort port = (IOPort) inputPortList.get(i);

                if (_refinementReferredInputPorts.contains(port)
                        && !_referredInputPorts.contains(port)) {
                    super.transferInputs(port);
                    controller._readInputs();
                    _referredInputPorts.add(port);
                }
            }

            // Fire the refinement.
            if (actors != null) {
                for (int i = 0; i < actors.length; ++i) {
                    if (_stopRequested) {
                        break;
                    }

                    if (actors[i].prefire()) {
                        actors[i].fire();
                        actors[i].postfire();
                    }
                }
            }

            controller._readOutputsFromRefinement();

            // Get inputs needed by the nonpreemptive transitions.
            getNonpreemptiveTransitionsReferredInputPorts(currentState);

            // Transfer additional inputs needed by the refinement.
            for (int i = 0; i < inputPortList.size(); i++) {
                IOPort port = (IOPort) inputPortList.get(i);

                if (_nonpreemptiveTransitionsInputs.contains(port)
                        && !_referredInputPorts.contains(port)) {
                    super.transferInputs(port);
                    controller._readInputs();
                    _referredInputPorts.add(port);
                }
            }

            // Choose a nonpreemptive transition.
            enabledTransitions = controller._checkTransition(currentState
                    .nonpreemptiveTransitionList());

            // Ensure that if there are multiple enabled transitions, all of them
            // must be nondeterministic.
            if (enabledTransitions.size() > 1) {
                Iterator transitions = enabledTransitions.iterator();

                while (transitions.hasNext()) {
                    Transition transition = (Transition) transitions.next();

                    if (!enabledTransition.isNondeterministic()) {
                        throw new MultipleEnabledTransitionsException(controller
                                .currentState(),
                                "Multiple enabled transitions found but "
                                + transition.getName() + " is deterministic.");
                    }
                }
            }

            // Randomly choose one transition from the list of the
            // enabled trnasitions.
            length = enabledTransitions.size();

            if (length != 0) {
                // Since the size of the list of enabled transitions usually (almost
                // always) is less than the maximum value of integer. We can safely
                // do the cast from long to int in the following statement.
                int randomChoice = (int) Math.floor(Math.random() * length);

                // There is tiny chance that randomChoice equals length.
                // When this happens, we deduct 1 from the randomChoice.
                if (randomChoice == length) {
                    randomChoice--;
                }

                enabledTransition = (Transition) enabledTransitions.get(randomChoice);
            }

            _enabledTransition = enabledTransition;
        }

        if (enabledTransition != null) {
            // Get additional inputs needed for output actions and set actions
            // of the enabled transition.
            getOutputActionsReferredInputPorts(enabledTransition);
            getSetActionsReferredInputPorts(enabledTransition);

            for (int i = 0; i < inputPortList.size(); i++) {
                IOPort port = (IOPort) inputPortList.get(i);

                if (_outputActionReferredInputPorts.contains(port)
                        && !_referredInputPorts.contains(port)) {
                    super.transferInputs(port);
                    controller._readInputs();
                    _referredInputPorts.add(port);
                }
            }

            controller._readInputs();

            // execute output actions.
            Iterator actions = enabledTransition.choiceActionList().iterator();

            while (actions.hasNext()) {
                Action action = (Action) actions.next();
                action.execute();
            }

            // Get additional input ports needed by set actions of
            // the enabeld transition.
            for (int i = 0; i < inputPortList.size(); i++) {
                IOPort port = (IOPort) inputPortList.get(i);

                if (_setActionReferredInputPorts.contains(port)
                        && !_referredInputPorts.contains(port)) {
                    super.transferInputs(port);
                    controller._readInputs();
                    _referredInputPorts.add(port);
                }
            }

            controller._readInputs();
        }

        controller._lastChosenTransition = enabledTransition;
    }

    /** Given a state, get a set of input ports referred in the guards of
     *  the preemptive transitions leaving that state.
     *  @param state The given state.
     *  @throws IllegalActionException If there is no controller or
     *   if any guard expression is illegal.
     */
    public void getNonpreemptiveTransitionsReferredInputPorts(State state)
            throws IllegalActionException {
        List nonpreemptiveTransitionList = state.nonpreemptiveTransitionList();

        _nonpreemptiveTransitionsInputs = getTransitionReferredInputPorts(nonpreemptiveTransitionList);
    }

    /** Given a transition, get a set of input ports referred in the
     *  outputActions of that transition.
     *  @param transition The transition.
     *  @exception IllegalActionException If there is no controller or if
     *  the outputActions is illegal.
     */
    public void getOutputActionsReferredInputPorts(Transition transition)
            throws IllegalActionException {
        _outputActionReferredInputPorts.clear();

        String string = transition.outputActions.getExpression();
        PtParser parser = new PtParser();
        ASTPtRootNode parseTree;
        ParseTreeFreeVariableCollector variableCollector = new ParseTreeFreeVariableCollector();
        FSMActor controller = getController();
        ParserScope scope = controller.getPortScope();

        if (!string.equals("")) {
            Map map = parser.generateAssignmentMap(string);
            Set set = new HashSet();

            for (Iterator names = map.keySet().iterator(); names.hasNext();) {
                String name = (String) names.next();

                ASTPtAssignmentNode node = (ASTPtAssignmentNode) map.get(name);
                parseTree = node.getExpressionTree();
                set = variableCollector.collectFreeVariables(parseTree, scope);
                getReferredInputPorts(set, _outputActionReferredInputPorts);
            }
        }
    }

    /** Given a state, get a set of input ports referred in the guards of
     *  the nonpreemptive transitions leaving that state.
     *  @param state The given state.
     *  @throws IllegalActionException If there is no controller or
     *   if any guard expression is illegal.
     */
    public void getPreemptiveTransitionsReferredInputPorts(State state)
            throws IllegalActionException {
        List preemptiveTransitionList = state.preemptiveTransitionList();

        _preemptiveTransitionsInputs = getTransitionReferredInputPorts(preemptiveTransitionList);
    }

    /** Given a set of ports, get those are input ports and put them
     *  in the indicated referred set.
     *  @param portSet The given set of ports
     *  @param referredInputPorts The referred set.
     */
    public void getReferredInputPorts(Set portSet, Set referredInputPorts) {
        CompositeActor container = (CompositeActor) getContainer();
        List inputPortList = container.inputPortList();

        for (int i = 0; i < inputPortList.size(); i++) {
            IOPort inputPort = (IOPort) inputPortList.get(i);

            if (portSet.contains(inputPort.getName())) {
                referredInputPorts.add(inputPort);
            }
        }
    }

    /** Given a state, get a set of input ports referred by the refinements
     *  of that state.
     * @param state The given state.
     * @throws IllegalActionException If refinement with given name is not
     *  found, or if the port rate does not contain a valid expression.
     */
    public void getRefinementReferredInputPorts(State state)
            throws IllegalActionException {
        _refinementReferredInputPorts.clear();

        TypedActor[] refinements = state.getRefinement();
        CompositeActor container = (CompositeActor) getContainer();

        if (refinements != null) {
            for (int i = 0; i < refinements.length; i++) {
                Iterator inputPorts = refinements[i].inputPortList().iterator();

                while (inputPorts.hasNext()) {
                    IOPort inputPort = (IOPort) inputPorts.next();

                    if ((inputPort.getWidth() != 0)
                            && (DFUtilities.getRate(inputPort) > 0)) {
                        Iterator inputPortsOutside = inputPort.deepConnectedInPortList()
                            .iterator();

                        while (inputPortsOutside.hasNext()) {
                            IOPort inputPortOutside = (IOPort) inputPortsOutside
                                .next();

                            if ((inputPortOutside.getContainer() == container)
                                    && !_refinementReferredInputPorts
                                    .contains(inputPortOutside)) {
                                _refinementReferredInputPorts.add(inputPortOutside);
                            }
                        }
                    }
                }
            }
        }
    }

    /** Given a transition, get a set of input ports referred in the set
     *  actions of that transition.
     *  @param transition The given transition.
     *  @throws IllegalActionException If there is no controller or
     *   if any set action expression is illegal.
     */
    public void getSetActionsReferredInputPorts(Transition transition)
            throws IllegalActionException {
        _setActionReferredInputPorts.clear();

        String string = transition.setActions.getExpression();
        PtParser parser = new PtParser();
        ASTPtRootNode parseTree;
        ParseTreeFreeVariableCollector variableCollector = new ParseTreeFreeVariableCollector();
        FSMActor controller = getController();
        ParserScope scope = controller.getPortScope();

        if (!string.equals("")) {
            Map map = parser.generateAssignmentMap(string);
            Set set = new HashSet();

            for (Iterator names = map.keySet().iterator(); names.hasNext();) {
                String name = (String) names.next();

                ASTPtAssignmentNode node = (ASTPtAssignmentNode) map.get(name);
                parseTree = node.getExpressionTree();
                set = variableCollector.collectFreeVariables(parseTree, scope);
                getReferredInputPorts(set, _setActionReferredInputPorts);
            }
        }
    }

    /** Given a list of transitions, get a set of referred input ports
     *  in the guard expressions of all the transitions leaving this state.
     *  @param transitionList The list of Transitions.
     *  @return A set of input ports referred by the guard expressions
     *   of the given transition list.
     *  @exception IllegalActionException If there is no controller or if
     *   the guard expression is illegal.
     */
    public Set getTransitionReferredInputPorts(List transitionList)
            throws IllegalActionException {
        Set transitionsReferredInputPorts = new HashSet();

        Iterator transitions = transitionList.iterator();

        while (transitions.hasNext()) {
            Transition transition = (Transition) transitions.next();
            String string = transition.getGuardExpression();

            if (string.equals("")) {
                throw new IllegalActionException(this,
                        "guard expression on " + transition.getName() + "is null!");
            }

            PtParser parser = new PtParser();
            ASTPtRootNode parseTree = parser.generateParseTree(string);
            ParseTreeFreeVariableCollector variableCollector = new ParseTreeFreeVariableCollector();
            FSMActor controller = getController();
            ParserScope scope = controller.getPortScope();
            Set set = variableCollector.collectFreeVariables(parseTree, scope);
            getReferredInputPorts(set, transitionsReferredInputPorts);
        }

        return transitionsReferredInputPorts;
    }

    /** Initialize the director. Get the referred input ports in the guard
     *  expressions of all preemptive transitions leaving the initial state.
     *  @exception IllegalActionException If the super class throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();

        FSMActor controller = getController();
        getPreemptiveTransitionsReferredInputPorts(controller.getInitialState());
        _referredInputPorts.clear();
        _referredInputPorts.addAll(_preemptiveTransitionsInputs);
    }

    /** Call the postfire() method of the super class. Get the referred
     *  input ports in the guard expressions of all preemptive transitions
     *  that go out from the current state.
     *  @exception IllegalActionException If the super class throws it.
     */
    public boolean postfire() throws IllegalActionException {
        boolean postfireValue = super.postfire();
        FSMActor controller = getController();
        getPreemptiveTransitionsReferredInputPorts(controller.currentState());
        _referredInputPorts.clear();
        _referredInputPorts.addAll(_preemptiveTransitionsInputs);
        return postfireValue;
    }

    /** Override the super class by only transferring inputs for those
     *  input ports that are referred by the guard expressions of the
     *  preemptive transitions leaving the current state.
     */
    public boolean transferInputs(IOPort port) throws IllegalActionException {
        if (_preemptiveTransitionsInputs.contains(port)) {
            return super.transferInputs(port);
        } else {
            return true;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // A set of input ports that are referred by the guard expressions
    // of the nonpreemptive transitions leaving the current state.
    private Set _nonpreemptiveTransitionsInputs = new HashSet();

    // A set of input ports that are referred by the output actions of
    // the enabled transition.
    private Set _outputActionReferredInputPorts = new HashSet();

    // A set of input ports that are referred by the guard expressions
    // of the preemptive transitions leaving the current state.
    private Set _preemptiveTransitionsInputs = new HashSet();

    // A set of input ports that are referred.
    private Set _referredInputPorts = new HashSet();

    // A set of input ports that are referred by the refinements
    // of the current state.
    private Set _refinementReferredInputPorts = new HashSet();

    // A set of input ports that are referred by the set actions of
    // the enabled transition.
    private Set _setActionReferredInputPorts = new HashSet();
}
