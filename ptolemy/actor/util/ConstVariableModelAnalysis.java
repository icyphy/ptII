/* An analysis that finds the constant variables in a ptolemy model

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
@ProposedRating Yellow (neuendor@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.actor.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Map;

import ptolemy.actor.parameters.PortParameter;
import ptolemy.actor.parameters.ParameterPort;
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
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.SingletonAttribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
//// ConstVariableModelAnalysis
/**
An analysis that traverses a model to determine all the constant
variables in a hierarchical model.  Basically, a constant variable in
a particular model is any variable in the model that is defined by a
expression of constants, or any variable that is defined by an
expression whose list of variables is contained within all the constant
variables in scope of that variable.

<p> This class computes the set of constant variables by computing the
set of variables that are not constant and then performing the
complement.  This is somewhat easier to compute.  The computation is
performed in two passes, the first of which extracts the set of
variables which must be not-constant either by not having an
expression, by inclusion in an initial set, by virtue of being a
PortParameter with an external connection, or by assignment from
within a modal model.  The second pass collects all the variables
which are not constant because they depend on other variables which
are not constant.  This class also recognizes dependence declarations
represented by the {@link DependencyDeclaration} class.

<p> This class also keeps track of the "change context" of each
dynamic variable.  The change context of a variable is an actor that
contains that variable.  During a firing of the actor, the variable's
value should not change.  This is important for supporting parameter
changes in the context of domains that perform scheduling based on
parameter values, like SDF.  The change context of a PortParameter
with an external connection is the container of the PortParameter.
The change context of a variable assigned by a finite state machine in
a modal model is the container of the finite state machine.  The
change context of asserted not constant variables and variables with
no expression are assumed to be the toplevel of the model.

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
            throws IllegalActionException, NameDuplicationException {
        this(model, Collections.EMPTY_SET);
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
    public ConstVariableModelAnalysis(
            Entity model, Set variableSet)
            throws IllegalActionException, NameDuplicationException {
        _containerToNotConstVariableSet = new HashMap();
        _containerToConstVariableSet = new HashMap();

        _variableToChangeContext = new HashMap();

        for(Iterator variables = variableSet.iterator();
            variables.hasNext();) {
            Variable variable = (Variable)variables.next();
            _variableToChangeContext.put(variable, model);
        }

        _collectNotConstantVariables(model, _variableToChangeContext);

        System.out.println("Analyzing constants");
        _analyzeAllVariables(model);
        System.out.println("Done analyzing constants");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the analysis that is active for the given object.
     */
    public static ConstVariableModelAnalysis getAnalysis(NamedObj object)
            throws IllegalActionException {
        Entity toplevel = (Entity)object.toplevel();
        ConstVariableModelAnalysis analysis = null;

        if(analysis == null) {
            try {
                analysis = new ConstVariableModelAnalysis(
                        toplevel);
            } catch (NameDuplicationException ex) {
                // Ignore
            }
        }
        return analysis;
    }

    /** Return the change context of the given variable.  This an
     *  actor containing the variable, such that the variable is
     *  guaraunteed not to change values during a firing of the actor.
     */
    public Entity getChangeContext(Variable variable) {
        return (Entity)_variableToChangeContext.get(variable);
    }

    /** Return the computed free variables for the given container.
     *  @exception RuntimeException If the constant variables for the
     *  container have not already been computed.
     */
    public Set getConstVariableNames(NamedObj container) {
        Set constVariables = (Set)_containerToConstVariableSet.get(container);
        if (constVariables == null) {
            throw new RuntimeException("Container " + container.getFullName() +
                    " has not been analyzed.");
        }

        return Collections.unmodifiableSet(constVariables);
    }

    /** Return the computed free variables for the given container.
     *  @exception RuntimeException If the constant variables for the
     *  container have not already been computed.
     */
    public Set getConstVariables(NamedObj container) {
        Set constVariables = (Set)_containerToConstVariableSet.get(container);
        if (constVariables == null) {
            throw new RuntimeException("Container " + container.getFullName() +
                    " has not been analyzed.");
        }

        Set set = new HashSet();
        for (Iterator names = constVariables.iterator();
             names.hasNext();) {
            set.add(container.getAttribute((String)names.next()));
        }
        return Collections.unmodifiableSet(set);
    }

    /** Return the computed free variables for the given container.
     *  @exception RuntimeException If the constant variables for the
     *  container have not already been computed.
     */
    public Set getNotConstVariableNames(NamedObj container) {
        Set variables = (Set)_containerToNotConstVariableSet.get(container);
        if (variables == null) {
            throw new RuntimeException("Container " + container.getFullName() +
                    " has not been analyzed.");
        }

        return Collections.unmodifiableSet(variables);
    }

    /** Return the computed free variables for the given container.
     *  @exception RuntimeException If the constant variables for the
     *  container have not already been computed.
     */
    public Set getNotConstVariables(NamedObj container) {
        Set variables = (Set)_containerToNotConstVariableSet.get(container);
        if (variables == null) {
            throw new RuntimeException("Container " + container.getFullName() +
                    " has not been analyzed.");
        }

        Set set = new HashSet();
        for (Iterator names = variables.iterator();
             names.hasNext();) {
            set.add(container.getAttribute((String)names.next()));
        }
        return Collections.unmodifiableSet(set);
    }

    ///////////////////////////////////////////////////////////////////
    ////                          private methods                  ////

    private static void _collectNotConstantVariables(
            FSMActor actor,
            AbstractActionsAttribute action,
            Map variableToChangeContext)
            throws IllegalActionException {
        for (Iterator names = action.getDestinationNameList().iterator();
             names.hasNext();) {
            String name = (String)names.next();
            NamedObj object = action.getDestination(name);
            if (object instanceof Variable) {
                // Note that the context of change is the modal model
                // container of the FSM.
                variableToChangeContext.put(object, actor.getContainer());
            }
        }
    }

    // Collect the set of variables in the given entity which might
    // change during execution.  This method adds an entry in the
    // given map from each dynamic parameter deeply contained in the
    // given entity to the change context of that parameter.
    private void _collectNotConstantVariables(Entity entity,
            Map variableToChangeContext)
            throws IllegalActionException {
        // All port parameters of an entity that have connections are
        // dynamic.  Their context of change is the actor that
        // contains the port parameter
        for (Iterator portParameters =
                 entity.attributeList(PortParameter.class).iterator();
             portParameters.hasNext();) {
            PortParameter parameter = (PortParameter)portParameters.next();
            ParameterPort port = parameter.getPort();

            // Under what conditions is a PortParameter not associated
            // with a port?  This came up in the context of
            // IterateOverArray.
            if(port != null && port.getWidth() > 0) {
                variableToChangeContext.put(parameter, entity);
            }
        }

        if (entity instanceof FSMActor) {
            // Collect assignments from FSM transitions
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
                        _collectNotConstantVariables(
                                (FSMActor) entity, action,
                                variableToChangeContext);
                    }
                    for (Iterator actions =
                             transition.commitActionList().iterator();
                         actions.hasNext();) {
                        AbstractActionsAttribute action =
                            (AbstractActionsAttribute)actions.next();
                        _collectNotConstantVariables(
                                (FSMActor) entity, action,
                                variableToChangeContext);
                    }
                }
            }
        } else if (entity instanceof CompositeEntity) {
            // Recurse through the whole model.
            CompositeEntity composite = (CompositeEntity)entity;
            for (Iterator entities = composite.entityList().iterator();
                 entities.hasNext();) {
                _collectNotConstantVariables((Entity)entities.next(),
                        variableToChangeContext);
            }
        }
    }

    // Recursively compute the set of const variables for all actors
    // deeply contained in the given model.
    private void _analyzeAllVariables(NamedObj container)
            throws IllegalActionException {
        // Sets of variables used to track the fixed point iteration.
        Set notTestedSet = new HashSet();
        Set testedSet = new HashSet();
        // Set of the names of constant attributes that we are computing.
        Set notConstants = new HashSet();

        // Initialize the work list to the set of attributes.
        List variableList = container.attributeList(Variable.class);
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
                NamedObj changeContext = null;

                // If the
                if (_variableToChangeContext.keySet().contains(variable)) {
                    isNotConstant = true;
                    changeContext = (NamedObj)
                        _variableToChangeContext.get(variable);
                }
                for(Iterator declarations =
                        variable.attributeList(DependencyDeclaration.class).iterator();
                    declarations.hasNext() && !isNotConstant;) {
                    DependencyDeclaration declaration =
                        (DependencyDeclaration) declarations.next();
                    for(Iterator dependents = declaration.getDependents().iterator();
                        dependents.hasNext() && !isNotConstant;) {
                        Variable scopeVariable = (Variable)dependents.next();
                        boolean scopeVariableChanges =
                            _variableToChangeContext.keySet().contains(
                                    scopeVariable);
                        if(scopeVariableChanges) {
                            isNotConstant = true;
                            changeContext = (NamedObj)
                                _variableToChangeContext.get(scopeVariable);
                        }
                    }
                }
                if(!isNotConstant) {
                    // Analyze the expression.
                    String expression = variable.getExpression();
                    // compute the variables.
                    try {
                        ASTPtRootNode root;
                        if(variable.isStringMode()) {
                            root = parser.generateStringParseTree(expression);
                        } else {
                            root = parser.generateParseTree(expression);
                        }
                        Set freeVarNames = new HashSet(
                                collector.collectFreeVariables(root));
                        for (Iterator names = freeVarNames.iterator();
                             names.hasNext() && !isNotConstant;) {
                            String name = (String)names.next();
                            Variable scopeVariable =
                                ModelScope.getScopedVariable(
                                        variable, variable, name);
                            boolean scopeVariableChanges =
                                scopeVariable != null &&
                                _variableToChangeContext.keySet().contains(
                                        scopeVariable);
                            if(scopeVariableChanges) {
                                isNotConstant = true;
                                changeContext = (NamedObj)
                                   _variableToChangeContext.get(scopeVariable);
                            } else if(scopeVariable == null) {
                                // Free variables (and methods) are
                                // usually assumed to be static since
                                // they will likely be bound to parser
                                // constants or looked up as Java
                                // methods.
                                if(!_assumeUnboundVariablesAreConstant) {
                                    // Free variables are assumed to be dynamic
                                    isNotConstant = true;
                                    changeContext = container.toplevel();
                                }
                            }
                        }
                    } catch (IllegalActionException ex) {
                        // Assume that this will be changed later...
                        // i.e. input_isPresent in FSM.

                        // Note that this also traps expressions that
                        // have no value as being variable...
                        isNotConstant = true;
                        changeContext = container;
                    }
                }
                if (isNotConstant) {
                    // Then the variable is also not constant.
                    notConstants.add(variable.getName());
                    _variableToChangeContext.put(variable, changeContext);
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

        _containerToNotConstVariableSet.put(container, notConstants);

        Set constants = new HashSet();
        for (Iterator variables = variableList.iterator();
             variables.hasNext();) {
            constants.add(((Variable)variables.next()).getName());
            constants.removeAll(notConstants);
        }
        _containerToConstVariableSet.put(container, constants);


        // recurse down.
        for (Iterator attributes = container.attributeList().iterator();
             attributes.hasNext();) {
            Attribute attribute = (Attribute)attributes.next();
            _analyzeAllVariables(attribute);
        }
        if (container instanceof CompositeEntity) {
            for (Iterator entities =
                     ((CompositeEntity)container).entityList().iterator();
                 entities.hasNext();) {
                Entity entity = (Entity)entities.next();
                _analyzeAllVariables(entity);
            }
        }
        if (container instanceof Entity) {
            for(Iterator ports =
                    ((Entity)container).portList().iterator();
                ports.hasNext();) {
                Port port = (Port)ports.next();
                _analyzeAllVariables(port);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private Map _containerToNotConstVariableSet;
    private Map _containerToConstVariableSet;
    private Map _variableToChangeContext;
    private CompositeActor _model;
    private static boolean _assumeUnboundVariablesAreConstant = true;
}
