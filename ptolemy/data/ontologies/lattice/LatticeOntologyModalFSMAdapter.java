/* An adapter class for ptolemy.domains.modal.kernel.FSMActor.

 Copyright (c) 2006-2014 The Regents of the University of California.
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
package ptolemy.data.ontologies.lattice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.ontologies.OntologyAdapter;
import ptolemy.data.ontologies.lattice.LatticeOntologySolver.ConstraintType;
import ptolemy.domains.modal.kernel.AbstractActionsAttribute;
import ptolemy.domains.modal.kernel.CommitActionsAttribute;
import ptolemy.domains.modal.kernel.FSMActor;
import ptolemy.domains.modal.kernel.OutputActionsAttribute;
import ptolemy.domains.modal.kernel.State;
import ptolemy.domains.modal.kernel.Transition;
import ptolemy.graph.Inequality;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
//// LatticeOntologyModelFSMAdapter

/** An adapter class for ptolemy.domains.modal.kernel.FSMActor.
 *
 *  @author Charles Shelton, Man-Kit Leung
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (mankit)
 *  @Pt.AcceptedRating Red (mankit)
 */
public class LatticeOntologyModalFSMAdapter extends
LatticeOntologyCompositeAdapter {

    /** Construct an adapter for the given FSMActor. This is the
     *  base adapter class for any FSMActor that does not have a
     *  specific defined adapter class. Default actor constraints
     *  are set for this adapter.
     *  @param solver The given solver.
     *  @param actor The given AtomicActor.
     *  @exception IllegalActionException Thrown if super class throws it.
     */
    public LatticeOntologyModalFSMAdapter(LatticeOntologySolver solver,
            FSMActor actor) throws IllegalActionException {
        super(solver, actor);
    }

    /** Return the list of constraints for this FSM.  They are a list of
     *  inequalities on the transition actions for each state in the FSM.
     *  @return A list of Inequality.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    @Override
    public List<Inequality> constraintList() throws IllegalActionException {
        // FIMXE: cannot call super here, because LatticeOntologyCompositeAdapter
        // recursively calls constraintList() of its contained components.
        // super.constraintList();

        HashMap<NamedObj, List<ASTPtRootNode>> outputActionMap = new HashMap<NamedObj, List<ASTPtRootNode>>();
        HashMap<NamedObj, List<ASTPtRootNode>> setActionMap = new HashMap<NamedObj, List<ASTPtRootNode>>();
        FSMActor actor = (ptolemy.domains.modal.kernel.FSMActor) getComponent();
        List propertyableList = getPropertyables();
        Iterator states = actor.entityList(State.class).iterator();

        while (states.hasNext()) {
            State state = (State) states.next();
            Iterator transitions = state.outgoingPort.linkedRelationList()
                    .iterator();

            while (transitions.hasNext()) {
                Transition transition = (Transition) transitions.next();
                Iterator propertyables = propertyableList.iterator();

                while (propertyables.hasNext()) {
                    Object propertyable = propertyables.next();
                    if (propertyable instanceof NamedObj) {

                        NamedObj namedObj = (NamedObj) propertyable;
                        OutputActionsAttribute outputActions = transition.outputActions;
                        if (outputActions.getDestinationNameList().contains(
                                namedObj.getName())) {
                            // do not consider multiple assignments to same output from one action since
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
                            // do not consider multiple assignments to same output from one action since
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

        boolean constrainSource = interconnectConstraintType == ConstraintType.EQUALS
                || interconnectConstraintType == ConstraintType.SOURCE_GE_SINK;

        Iterator outputActions = outputActionMap.entrySet().iterator();
        while (outputActions.hasNext()) {
            Entry entry = (Entry) outputActions.next();
            Object destination = entry.getKey();
            List<Object> expressions = (List<Object>) entry.getValue();

            if (constrainSource) {
                Iterator roots = expressions.iterator();

                while (roots.hasNext()) {
                    ASTPtRootNode root = (ASTPtRootNode) roots.next();
                    List<Object> sinkAsList = new ArrayList<Object>();
                    sinkAsList.add(destination);

                    _constrainObject(interconnectConstraintType, root,
                            sinkAsList);
                }
            } else {
                _constrainObject(interconnectConstraintType, destination,
                        expressions);
            }
        }

        Iterator setActions = setActionMap.entrySet().iterator();
        while (setActions.hasNext()) {
            Entry entry = (Entry) setActions.next();
            Object destination = entry.getKey();
            List<Object> expressions = (List<Object>) entry.getValue();

            if (constrainSource) {
                Iterator roots = expressions.iterator();

                while (roots.hasNext()) {
                    ASTPtRootNode root = (ASTPtRootNode) roots.next();
                    List<Object> sinkAsList = new ArrayList<Object>();
                    sinkAsList.add(destination);

                    _constrainObject(interconnectConstraintType, root,
                            sinkAsList);
                }
            } else {
                _constrainObject(interconnectConstraintType, destination,
                        expressions);
            }
        }

        return _union(_ownConstraints, _subAdapterConstraints);
    }

    /** Return the list of parse tree root nodes that correspond to the
     *  specified state's outgoing transition actions in the FSM.
     *  @param state The state from which to get the parse trees.
     *  @return The list of parse trees.
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

    /** Return the list of parse tree root nodes that correspond to all
     *  attributes contained in the outgoing transitions from the states
     *  in the FSM.
     *  @return The list of parse trees.
     *  @exception IllegalActionException Thrown if there is a problem getting
     *   the parse tree root nodes.
     */
    @Override
    protected List<ASTPtRootNode> _getAttributeParseTrees()
            throws IllegalActionException {
        List<ASTPtRootNode> result = super._getAttributeParseTrees();
        FSMActor actor = (FSMActor) getComponent();

        Iterator states = actor.entityList(State.class).iterator();
        while (states.hasNext()) {
            State state = (State) states.next();
            result.addAll(getParseTrees(state));
        }
        return result;
    }

    /** Get the list of propertyable attributes for this adapter.
     *  In this base adapter class for FSM, it considers all guard
     *  expressions as propertyable attributes.
     *  @return The list of propertyable attributes.
     */
    @Override
    protected List<Attribute> _getPropertyableAttributes() {
        List<Attribute> result = super._getPropertyableAttributes();
        FSMActor actor = (FSMActor) getComponent();

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

    /** Return the list of sub-adapters. In this base class, it
     *  returns the list of ASTNode adapters that are associated
     *  with the expressions of the propertyable attributes.
     *  @return The list of sub-adapters.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    @Override
    protected List<OntologyAdapter> _getSubAdapters()
            throws IllegalActionException {
        return _getASTNodeAdapters();
    }

    /** Return the parse tree that corresponds with actions in a state or
     *   transition.
     *  @param actions The attribute that contains the list of actions.
     *  @return The parse tree.
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
