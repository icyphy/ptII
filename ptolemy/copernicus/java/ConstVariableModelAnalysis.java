/* An analysis that finds the free variables in a ptolemy model

 Copyright (c) 2001-2003 The Regents of the University of California.
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
@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.copernicus.java;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import ptolemy.actor.CompositeActor;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.ModelScope;
import ptolemy.data.expr.ParseTreeFreeVariableCollector;
import ptolemy.data.expr.PtParser;
import ptolemy.data.expr.Variable;
import ptolemy.domains.fsm.kernel.AbstractActionsAttribute;
import ptolemy.domains.fsm.kernel.FSMActor;
import ptolemy.domains.fsm.kernel.State;
import ptolemy.domains.fsm.kernel.Transition;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
//// ConstVariableModelAnalysis
/**
An analysis that traverses a model to determine all the constant
variables in a hierarchical model.  Basically, a constant variable in
a particular model is any variable in the model that is defined by a
expression of constants, or any variable that is defined by an
expression whos list of variables is contained within all the constant
variables in scope of that variable.

<p> This class computes the set of constant variables by computing the
set of variables that are not constant and then performing the
complement.  This is somewhat easier to compute.  The computation is
performed in two passes, the first of which extracts the set of
variables which must be not-constant either by inclusion in an initial
set, or by assignment from within a modal model.  The second pass
collects all the variables which are not constant because they depend
on other variables which are not constant.

@author Stephen Neuendorffer
@version $Id$
@since Ptolemy II 2.0
*/
public class ConstVariableModelAnalysis {

    /** Analyze the given model to determine which variables must be
     *  constants and which variables may change dynamically during
     *  execution.  In addition, store the intermediate results for
     *  contained actors so they can be retrieved by the
     *  getConstVariables() method.
     *  @exception IllegalActionException If an exception occurs
     *  during analysis.
     */
    public ConstVariableModelAnalysis(Entity model)
            throws IllegalActionException {
        this(model, new HashSet());
    }

    /** Analyze the given model to determine which variables must be
     *  constants and which variables may change dynamically during
     *  execution, given that all variables in the given set may
     *  change dynamically.  In addition, store the intermediate
     *  results for contained actors so they can be retrieved by the
     *  getConstVariables() method.
     *  @exception IllegalActionException If an exception occurs
     *  during analysis.
     */
    public ConstVariableModelAnalysis(Entity model, Set variableSet)
            throws IllegalActionException {
        _entityToNotConstVariableSet = new HashMap();
        _entityToConstVariableSet = new HashMap();
        _notConstantVariableSet = new HashSet(variableSet);
        _collectNotConstantVariables(model, _notConstantVariableSet);

        _analyzeAllVariables(model);
    }

    /** Return the computed free variables for the given entity.
     *  @exception RuntimeException If the constant variables for the
     *  entity have not already been computed.
     */
    public Set getConstVariableNames(Entity entity) {
        Set constVariables = (Set)_entityToConstVariableSet.get(entity);
        if (constVariables == null) {
            throw new RuntimeException("Entity " + entity.getFullName() +
                    " has not been analyzed.");
        }

        return Collections.unmodifiableSet(constVariables);
    }

    /** Return the computed free variables for the given entity.
     *  @exception RuntimeException If the constant variables for the
     *  entity have not already been computed.
     */
    public Set getConstVariables(Entity entity) {
        Set constVariables = (Set)_entityToConstVariableSet.get(entity);
        if (constVariables == null) {
            throw new RuntimeException("Entity " + entity.getFullName() +
                    " has not been analyzed.");
        }

        Set set = new HashSet();
        for (Iterator names = constVariables.iterator();
             names.hasNext();) {
            set.add(entity.getAttribute((String)names.next()));
        }
        return Collections.unmodifiableSet(set);
    }

    /** Return the computed free variables for the given entity.
     *  @exception RuntimeException If the constant variables for the
     *  entity have not already been computed.
     */
    public Set getNotConstVariableNames(Entity entity) {
        Set variables = (Set)_entityToNotConstVariableSet.get(entity);
        if (variables == null) {
            throw new RuntimeException("Entity " + entity.getFullName() +
                    " has not been analyzed.");
        }

        return Collections.unmodifiableSet(variables);
    }

    /** Return the computed free variables for the given entity.
     *  @exception RuntimeException If the constant variables for the
     *  entity have not already been computed.
     */
    public Set getNotConstVariables(Entity entity) {
        Set variables = (Set)_entityToNotConstVariableSet.get(entity);
        if (variables == null) {
            throw new RuntimeException("Entity " + entity.getFullName() +
                    " has not been analyzed.");
        }

        Set set = new HashSet();
        for (Iterator names = variables.iterator();
             names.hasNext();) {
            set.add(entity.getAttribute((String)names.next()));
        }
        return Collections.unmodifiableSet(set);
    }

    private static void _collectNotConstantVariables(
            AbstractActionsAttribute action, Set set)
            throws IllegalActionException {
        for (Iterator names = action.getDestinationNameList().iterator();
             names.hasNext();) {
            String name = (String)names.next();
            NamedObj object = action.getDestination(name);
            if (object instanceof Variable) {
                set.add(object);
            }
        }
    }

    // Collect the set of variables in the given entity which might
    // change during execution.
    private void _collectNotConstantVariables(Entity entity, Set set)
            throws IllegalActionException {
        if (entity instanceof FSMActor) {
            for (Iterator states = ((FSMActor)entity).entityList().iterator();
                 states.hasNext();) {
                State state = (State)states.next();
                for (Iterator transitions =
                         state.outgoingPort.linkedRelationList().iterator();
                     transitions.hasNext();) {
                    Transition transition = (Transition)transitions.next();
                    for (Iterator actions =
                             transition.choiceActionList().iterator();
                         actions.hasNext();) {
                        AbstractActionsAttribute action =
                            (AbstractActionsAttribute)actions.next();
                        _collectNotConstantVariables(action, set);
                    }
                    for (Iterator actions =
                             transition.commitActionList().iterator();
                         actions.hasNext();) {
                        AbstractActionsAttribute action =
                            (AbstractActionsAttribute)actions.next();
                        _collectNotConstantVariables(action, set);
                    }
                }
            }
        } else if (entity instanceof CompositeEntity) {
            CompositeEntity composite = (CompositeEntity)entity;
            for (Iterator entities = composite.entityList().iterator();
                 entities.hasNext();) {
                _collectNotConstantVariables((Entity)entities.next(), set);
            }
        }
    }

    // Recursively compute the set of const variables for all actors
    // deeply contained in the given model.
    private void _analyzeAllVariables(Entity model)
            throws IllegalActionException {
        // Sets of variables used to track the fixed point iteration.
        Set notTestedSet = new HashSet();
        Set testedSet = new HashSet();
        // Set of the names of constant attributes that we are computing.
        Set notConstants = new HashSet();

        // initialize the work list to the set of attributes.
        List variableList = model.attributeList(Variable.class);
        notTestedSet.addAll(variableList);

        PtParser parser = new PtParser();
        ParseTreeFreeVariableCollector collector =
            new ParseTreeFreeVariableCollector();

        // The fixed point of the constant set.
        boolean doneSomething = true;
        while (doneSomething) {
            doneSomething = false;
            while (!notTestedSet.isEmpty()) {
                Variable variable = (Variable)notTestedSet.iterator().next();
                notTestedSet.remove(variable);

                // Perform the test.
                boolean isNotConstant = false;
                if (_notConstantVariableSet.contains(variable)) {
                    isNotConstant = true;
                } else {
                    // Analyze the expression.
                    String expression = variable.getExpression();
                    // compute the variables.
                    try {
                        ASTPtRootNode root =
                            parser.generateParseTree(expression);
                        Set freeVarNames = new HashSet(
                                collector.collectFreeVariables(root));
                        for (Iterator names = freeVarNames.iterator();
                             names.hasNext() && !isNotConstant;) {
                            String name = (String)names.next();
                            Variable scopeVariable =
                                ModelScope.getScopedVariable(
                                        variable, variable, name);
                         
                            if(_assumeParserConstantsAreConstant) {
                                // Free variables (i.e. methods) bound
                                // to parser constants are assumed to
                                // be static.
                                if (scopeVariable != null &&
                                        _notConstantVariableSet.contains(scopeVariable)) {
                                    isNotConstant = true;
                                }
                                if (scopeVariable == null && 
                                        ptolemy.data.expr.Constants.get(name) == null) {
                                    isNotConstant = true;
                                }
                            } else {
                                // Free variables are assumed to be dynamic
                                if(scopeVariable == null ||
                                        _notConstantVariableSet.contains(scopeVariable)) {
                                    isNotConstant = true;
                                }
                            }

                        }
                    } catch (IllegalActionException ex) {
                        // Assume that this will be changed later...
                        // i.e. input_isPresent in FSM.
                        isNotConstant = true;
                    }
                }
                if (isNotConstant) {
                    // Then the variable is also not constant.
                    notConstants.add(variable.getName());
                    _notConstantVariableSet.add(variable);
                    doneSomething = true;
                    isNotConstant = true;
                } else {
                    // It may still be constant, so add it back to the pool.
                    testedSet.add(variable);
                }
            }
            // Reset worklist for next iteration.
            notTestedSet.addAll(testedSet);
            testedSet.clear();
        }

        _entityToNotConstVariableSet.put(model, notConstants);

        Set constants = new HashSet();
        for (Iterator variables = variableList.iterator();
             variables.hasNext();) {
            constants.add(((Variable)variables.next()).getName());
            constants.removeAll(notConstants);
        }
        _entityToConstVariableSet.put(model, constants);


        // recurse down.
        if (model instanceof CompositeEntity) {
            for (Iterator entities =
                     ((CompositeEntity)model).entityList().iterator();
                 entities.hasNext();) {
                Entity entity = (Entity)entities.next();
                _analyzeAllVariables(entity);
            }
        }
    }

    private HashSet _notConstantVariableSet;
    private HashMap _entityToNotConstVariableSet;
    private HashMap _entityToConstVariableSet;
    private CompositeActor _model;
    private static boolean _assumeParserConstantsAreConstant = true;
}
