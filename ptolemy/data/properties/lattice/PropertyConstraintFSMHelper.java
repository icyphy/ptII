/* A helper class for ptolemy.actor.AtomicActor.

 Copyright (c) 2006 The Regents of the University of California.
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

//////////////////////////////////////////////////////////////////////////
//// FSMActor

/**
 A helper class for ptolemy.actor.FSMActor.

 @author Man-Kit Leung, Thomas Mandl
 @version $Id$
 @since Ptolemy II 6.2
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
 */
public class PropertyConstraintFSMHelper extends PropertyConstraintCompositeHelper {

    /**
     * Construct a helper for the given AtomicActor. This is the
     * helper class for any ActomicActor that does not have a
     * specific defined helper class. Default actor constraints
     * are set for this helper. 
     * @param actor The given ActomicActor.
     * @param lattice The staticDynamic lattice.
     * @throws IllegalActionException 
     */
    public PropertyConstraintFSMHelper(PropertyConstraintSolver solver, 
            ptolemy.domains.fsm.kernel.FSMActor actor)
            throws IllegalActionException {
        
        super(solver, actor);
    }
    
    public List<Inequality> constraintList() throws IllegalActionException {
        super.constraintList();
        
        HashMap<NamedObj, List<ASTPtRootNode>> outputActionMap = 
            new HashMap<NamedObj, List<ASTPtRootNode>>();
        
        HashMap<NamedObj, List<ASTPtRootNode>> setActionMap = 
            new HashMap<NamedObj, List<ASTPtRootNode>>();

        ptolemy.domains.fsm.kernel.FSMActor actor = 
            (ptolemy.domains.fsm.kernel.FSMActor) getComponent();

        List propertyableList = getPropertyables();
                
        Iterator states = actor.entityList(State.class).iterator();
        
        while (states.hasNext()) {
            State state = (State) states.next();
            Iterator transitions = 
                state.outgoingPort.linkedRelationList().iterator();

            while (transitions.hasNext()) {
                Transition transition = (Transition) transitions.next();
                
                Iterator propertyables = propertyableList.iterator();
                
                while (propertyables.hasNext()) {
                    Object propertyable = propertyables.next();
                    if (propertyable instanceof NamedObj) {

                        NamedObj namedObj = (NamedObj) propertyable;

                        OutputActionsAttribute outputActions = transition.outputActions;                        
                        if (outputActions.getDestinationNameList().contains(namedObj.getName())) {
                            // do not consider multiple assigenments to same output from one action since
                            // only the last assignment in the expression is actually effective                            
                            ASTPtRootNode parseTree = outputActions.getParseTree(namedObj.getName());                        
                            if (!outputActionMap.containsKey(namedObj)) {
                                outputActionMap.put(namedObj, new ArrayList<ASTPtRootNode>());                        
                            } 
                            outputActionMap.get(namedObj).add(parseTree);
                        }

                        CommitActionsAttribute setActions = transition.setActions;                        
                        if (setActions.getDestinationNameList().contains(namedObj.getName())) {
                            // do not consider multiple assigenments to same output from one action since
                            // only the last assignment in the expression is actually effective                            
                            ASTPtRootNode parseTree = setActions.getParseTree(namedObj.getName());                        
                            if (!setActionMap.containsKey(namedObj)) {
                                setActionMap.put(namedObj, new ArrayList<ASTPtRootNode>());                        
                            } 
                            setActionMap.get(namedObj).add(parseTree);
                        }
                    }
                }
            }
        }

        boolean constraintSource = 
            (interconnectConstraintType == ConstraintType.SRC_EQUALS_MEET) ||  
            (interconnectConstraintType == ConstraintType.SRC_EQUALS_GREATER);

        Iterator outputActions = outputActionMap.entrySet().iterator();        
        while (outputActions.hasNext()) {
            Entry entry = (Entry) outputActions.next();
            Object destination = entry.getKey();
            List<Object> expressions = 
                (List<Object>) entry.getValue();
            
            if (constraintSource) {
                Iterator roots = expressions.iterator();
                
                while (roots.hasNext()) {
                    ASTPtRootNode root = (ASTPtRootNode) roots.next();
                    List<Object> sinkAsList = new ArrayList<Object>();
                    sinkAsList.add(destination);
                    
                    _constraintObject(interconnectConstraintType, root, sinkAsList);            
                }
            } else {
                _constraintObject(interconnectConstraintType, destination, expressions);            
            }            
        }        
        
        Iterator setActions = setActionMap.entrySet().iterator();        
        while (setActions.hasNext()) {
            Entry entry = (Entry) setActions.next();
            Object destination = entry.getKey();
            List<Object> expressions = 
                (List<Object>) entry.getValue();
            
            if (constraintSource) {
                Iterator roots = expressions.iterator();
                
                while (roots.hasNext()) {
                    ASTPtRootNode root = (ASTPtRootNode) roots.next();
                    List<Object> sinkAsList = new ArrayList<Object>();
                    sinkAsList.add(destination);
                    
                    _constraintObject(interconnectConstraintType, root, sinkAsList);            
                }
            } else {
                _constraintObject(interconnectConstraintType, destination, expressions);            
            }            
        }        
        _checkIneffectiveOutputPorts(actor, outputActionMap.keySet(), setActionMap.keySet());
        
        return _union(_ownConstraints, _subHelperConstraints);
    }    
    
    private void _checkIneffectiveOutputPorts(
            FSMActor actor, 
            Set<NamedObj> setDestinations1,
            Set<NamedObj> setDestinations2) {
        
        Iterator outputs = actor.outputPortList().iterator();
        while (outputs.hasNext()) {
            IOPort output = (IOPort) outputs.next();
            if ((!setDestinations1.isEmpty()) && (!setDestinations2.isEmpty())) {
                if ((!setDestinations1.contains(output)) && (!setDestinations2.contains(output))) {
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

    
    /**
     * 
     */
    protected List<ASTPtRootNode> _getAttributeParseTrees() {
        List<ASTPtRootNode> result = super._getAttributeParseTrees();

        ptolemy.domains.fsm.kernel.FSMActor actor = 
            (ptolemy.domains.fsm.kernel.FSMActor) getComponent();

        Iterator states = actor.entityList(State.class).iterator();
        while (states.hasNext()) {
            State state = (State) states.next();
        
            result.addAll(getParseTrees(state));
        }
        return result;
    }

    public List<ASTPtRootNode> getParseTrees(State state) {
        List<ASTPtRootNode> result = new LinkedList<ASTPtRootNode>();
        Iterator transitions = 
            state.outgoingPort.linkedRelationList().iterator();

        while (transitions.hasNext()) {
            Transition transition = (Transition) transitions.next();
            
            result.addAll(_getParseTrees(transition.outputActions));
            result.addAll(_getParseTrees(transition.setActions));
        }
        return result;
    }
    
    /**
     * 
     */
    
    /**
     * @param actions
     * @return
     */
    private List<ASTPtRootNode> _getParseTrees(AbstractActionsAttribute actions) {
        List<ASTPtRootNode> parseTrees = actions.getParseTreeList();
        
        Iterator iterator = parseTrees.iterator();
        while (iterator.hasNext()) {
            putAttributes((ASTPtRootNode) iterator.next(), actions);
        }
        return parseTrees;
    }
    
    /**
     * 
     * @return
     */
    protected List<Attribute> _getPropertyableAttributes() {
        List<Attribute> result = super._getPropertyableAttributes();

        ptolemy.domains.fsm.kernel.FSMActor actor = 
            (ptolemy.domains.fsm.kernel.FSMActor) getComponent();

        Iterator states = actor.entityList(State.class).iterator();
        while (states.hasNext()) {
            State state = (State) states.next();

            Iterator transitions = 
                state.outgoingPort.linkedRelationList().iterator();

            while (transitions.hasNext()) {
                Transition transition = (Transition) transitions.next();
                result.add(transition.guardExpression);
            }
        }

        return result;
    }

    /**
     * 
     * @return
     * @throws IllegalActionException
     */
    protected List<PropertyHelper> _getSubHelpers() throws IllegalActionException {
        return _getASTNodeHelpers();
    }

}
