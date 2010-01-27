/*
 * An adapter class for ptolemy.domains.fsm.kernel.FSMActor.
 * 
 * Copyright (c) 2006-2010 The Regents of the University of California. All
 * rights reserved. Permission is hereby granted, without written agreement and
 * without license or royalty fees, to use, copy, modify, and distribute this
 * software and its documentation for any purpose, provided that the above
 * copyright notice and the following two paragraphs appear in all copies of
 * this software.
 * 
 * IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY FOR
 * DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT
 * OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE UNIVERSITY OF
 * CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE PROVIDED HEREUNDER IS ON AN
 * "AS IS" BASIS, AND THE UNIVERSITY OF CALIFORNIA HAS NO OBLIGATION TO PROVIDE
 * MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
 * 
 * PT_COPYRIGHT_VERSION_2 COPYRIGHTENDKEY
 * 
 */
package ptolemy.data.properties.lattice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import ptolemy.actor.IOPort;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.properties.PropertyHelper;
import ptolemy.data.properties.lattice.PropertyConstraintSolver.ConstraintType;
import ptolemy.domains.fsm.kernel.AbstractActionsAttribute;
import ptolemy.domains.fsm.kernel.CommitActionsAttribute;
import ptolemy.domains.fsm.kernel.FSMActor;
import ptolemy.domains.fsm.kernel.OutputActionsAttribute;
import ptolemy.domains.fsm.kernel.State;
import ptolemy.domains.fsm.kernel.Transition;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// FSMActor

/**
 * An adapter class for ptolemy.domains.fsm.kernel.FSMActor.
 * 
 * @author Man-Kit Leung, Thomas Mandl
 * @version $Id: PropertyConstraintFSMHelper.java 54803 2009-06-29 22:33:45Z
 * mankit $
 * @since Ptolemy II 8.0
 * @Pt.ProposedRating Red (mankit)
 * @Pt.AcceptedRating Red (mankit)
 */
public class PropertyConstraintFSMHelper extends
        PropertyConstraintCompositeHelper {

    /**
     * Construct an adapter for the given FSMActor. This is the base adapter class
     * for any FSMActor that does not have a specific defined adapter class.
     * Default actor constraints are set for this adapter.
     * @param solver The given solver.
     * @param actor The given ActomicActor.
     * @exception IllegalActionException Thrown if super class throws it.
     */
    public PropertyConstraintFSMHelper(PropertyConstraintSolver solver,
            ptolemy.domains.fsm.kernel.FSMActor actor)
            throws IllegalActionException {

        super(solver, actor);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Return the list of property constraints. Return the constraints for the
     * setAction and outputAction expressions associated with transitions of
     * each contained states. For example, if there is a setAction for assigning
     * the value of the variable V, this creates constraints between the
     * properties of V and the assigned expression.
     * @return The list of property constraints.
     * @exception IllegalActionException Thrown if any error occurs when
     * creating the constraints.
     */
    public List<Inequality> constraintList() throws IllegalActionException {
        // FIMXE: cannot call super here, because PropertyConstraintCompositeHelper
        // recursively call constraintList() of its children.
        //super.constraintList();

        ptolemy.domains.fsm.kernel.FSMActor actor = (ptolemy.domains.fsm.kernel.FSMActor) getComponent();
        HashMap<NamedObj, List<ASTPtRootNode>> outputActionMap = new HashMap<NamedObj, List<ASTPtRootNode>>();
        HashMap<NamedObj, List<ASTPtRootNode>> setActionMap = new HashMap<NamedObj, List<ASTPtRootNode>>();

        for (State state : (List<State>) actor.entityList(State.class)) {
            List<Transition> transitions = state.outgoingPort
                    .linkedRelationList();

            for (Transition transition : transitions) {
                for (Object propertyable : getPropertyables()) {

                    if (propertyable instanceof NamedObj) {

                        NamedObj namedObj = (NamedObj) propertyable;

                        OutputActionsAttribute outputActions = transition.outputActions;
                        if (outputActions.getDestinationNameList().contains(
                                namedObj.getName())) {
                            // do not consider multiple assigenments to same output from one action since
                            // only the last assignment in the expression is actually effective
                            ASTPtRootNode parseTree = outputActions
                                    .getParseTree(namedObj.getName());
                            if (!outputActionMap.containsKey(namedObj)) {
                                outputActionMap.put(namedObj,
                                        new ArrayList<ASTPtRootNode>());
                            }
                            outputActionMap.get(namedObj).add(parseTree);
                        }

                        CommitActionsAttribute setActions = transition.setActions;
                        if (setActions.getDestinationNameList().contains(
                                namedObj.getName())) {
                            // do not consider multiple assigenments to same output from one action since
                            // only the last assignment in the expression is actually effective
                            ASTPtRootNode parseTree = setActions
                                    .getParseTree(namedObj.getName());
                            if (!setActionMap.containsKey(namedObj)) {
                                setActionMap.put(namedObj,
                                        new ArrayList<ASTPtRootNode>());
                            }
                            setActionMap.get(namedObj).add(parseTree);
                        }
                    }
                }
            }
        }

        boolean constraintSource = interconnectConstraintType == ConstraintType.SRC_EQUALS_MEET
                || interconnectConstraintType == ConstraintType.SRC_EQUALS_GREATER;

        for (Entry entry : outputActionMap.entrySet()) {
            Object destination = entry.getKey();

            List<ASTPtRootNode> expressions = (List<ASTPtRootNode>) entry
                    .getValue();

            if (constraintSource) {

                for (ASTPtRootNode root : expressions) {
                    List<Object> sinkAsList = new ArrayList<Object>();
                    sinkAsList.add(destination);

                    _constraintObject(interconnectConstraintType, root,
                            sinkAsList);
                }
            } else {
                _constraintObject(interconnectConstraintType, destination,
                        expressions);
            }
        }

        for (Entry entry : setActionMap.entrySet()) {
            Object destination = entry.getKey();
            List<ASTPtRootNode> expressions = (List<ASTPtRootNode>) entry
                    .getValue();

            if (constraintSource) {

                for (ASTPtRootNode root : expressions) {
                    List<Object> sinkAsList = new ArrayList<Object>();
                    sinkAsList.add(destination);

                    _constraintObject(interconnectConstraintType, root,
                            sinkAsList);
                }
            } else {
                _constraintObject(interconnectConstraintType, destination,
                        expressions);
            }
        }
        _checkIneffectiveOutputPorts(actor, outputActionMap.keySet(),
                setActionMap.keySet());

        return _union(_ownConstraints, _subHelperConstraints);
    }

    /**
     * Return the list of parsed expression trees for the specified State. This
     * traverses the outgoing transitions of the State. The outputActions and
     * setActions attributes are parsed for each of these transitions and
     * included into the return list.
     * @param state The specified State.
     * @return the list of parse trees for the specified State.
     */
    public List<ASTPtRootNode> getParseTrees(State state) {
        List<ASTPtRootNode> result = new LinkedList<ASTPtRootNode>();
        Iterator transitions = state.outgoingPort.linkedRelationList()
                .iterator();

        while (transitions.hasNext()) {
            Transition transition = (Transition) transitions.next();

            result.addAll(_getParseTrees(transition.outputActions));
            result.addAll(_getParseTrees(transition.setActions));
        }
        return result;
    }

    /**
     * Constrain the first specified term to be at least the second term (e.g.
     * term1 >= term2). If neither of the terms is null, increment the
     * statistics for default constraints set up.
     */
    public void setAtLeastByDefault(Object term1, Object term2) {
        setAtLeast(term1, term2);

        if (term1 != null && term2 != null) {
            _solver.incrementStats("# of default constraints", 1);
            _solver.incrementStats("# of composite default constraints", 1);
        }
    }

    /**
     * Constrain the two specified terms same as each other. If neither of the
     * terms is null, increment the statistics for default constraints set up.
     */
    public void setSameAsByDefault(Object term1, Object term2) {
        setSameAs(term1, term2);

        if (term1 != null && term2 != null) {
            _solver.incrementStats("# of default constraints", 2);
            _solver.incrementStats("# of composite default constraints", 2);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /**
     * Return a list of root nodes for the parse trees of attribute expressions.
     * This also considers the attribute expressions of all the States contained
     * in this FSM.
     * @return A list of ASTPtRootNodes.
     * @exception IllegalActionException If the super method throws it.
     */
    protected List<ASTPtRootNode> _getAttributeParseTrees()
            throws IllegalActionException {
        List<ASTPtRootNode> result = super._getAttributeParseTrees();

        ptolemy.domains.fsm.kernel.FSMActor actor = (ptolemy.domains.fsm.kernel.FSMActor) getComponent();

        Iterator states = actor.entityList(State.class).iterator();
        while (states.hasNext()) {
            State state = (State) states.next();

            result.addAll(getParseTrees(state));
        }
        return result;
    }

    /**
     * Get the list of propertyable attributes for this adapter. In this base
     * adapter class for FSM, it considers all guard expressions as propertyable
     * attributes.
     * @return The list of propertyable attributes.
     */
    protected List<Attribute> _getPropertyableAttributes() {
        List<Attribute> result = super._getPropertyableAttributes();

        ptolemy.domains.fsm.kernel.FSMActor actor = (ptolemy.domains.fsm.kernel.FSMActor) getComponent();

        Iterator states = actor.entityList(State.class).iterator();
        while (states.hasNext()) {
            State state = (State) states.next();

            Iterator transitions = state.outgoingPort.linkedRelationList()
                    .iterator();

            while (transitions.hasNext()) {
                Transition transition = (Transition) transitions.next();
                result.add(transition.guardExpression);
            }
        }

        return result;
    }

    /**
     * Return the list of sub-adapters. In this base class, it returns the list
     * of ASTNode adapters that are associated with the expressions of the
     * propertyable attributes.
     * @return The list of sub-adapters.
     * @exception IllegalActionException Not thrown in this base class.
     */
    protected List<PropertyHelper> _getSubHelpers()
            throws IllegalActionException {
        return _getASTNodeHelpers();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    private void _checkIneffectiveOutputPorts(FSMActor actor,
            Set<NamedObj> setDestinations1, Set<NamedObj> setDestinations2) {

        Iterator outputs = actor.outputPortList().iterator();
        while (outputs.hasNext()) {
            IOPort output = (IOPort) outputs.next();
            if (!setDestinations1.isEmpty() && !setDestinations2.isEmpty()) {
                if (!setDestinations1.contains(output)
                        && !setDestinations2.contains(output)) {
                    getPropertyTerm(output).setEffective(false);
                }
            } else if (setDestinations1.isEmpty()) {
                if (!setDestinations2.contains(output)) {
                    getPropertyTerm(output).setEffective(false);
                }
            } else if (setDestinations2.isEmpty()) {
                if (!setDestinations1.contains(output)) {
                    getPropertyTerm(output).setEffective(false);
                }
            }
        }
    }

    /** Return the parse trees for a given action.
     * @param actions The given action.
     * @return The parse trees.
     */
    private List<ASTPtRootNode> _getParseTrees(AbstractActionsAttribute actions) {
        List<ASTPtRootNode> parseTrees = actions.getParseTreeList();

        Iterator iterator = parseTrees.iterator();
        while (iterator.hasNext()) {
            putAttribute((ASTPtRootNode) iterator.next(), actions);
        }
        return parseTrees;
    }

}
