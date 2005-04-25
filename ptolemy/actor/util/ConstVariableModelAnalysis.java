/* An analysis that finds the constant variables in a ptolemy model

Copyright (c) 2003-2005 The Regents of the University of California.
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
package ptolemy.actor.util;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Manager;
import ptolemy.actor.parameters.ParameterPort;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.Token;
import ptolemy.data.expr.ModelScope;
import ptolemy.data.expr.Variable;
import ptolemy.graph.DirectedGraph;
import ptolemy.graph.Edge;
import ptolemy.graph.Node;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;


//////////////////////////////////////////////////////////////////////////
//// ConstVariableModelAnalysis

/**
   An analysis that traverses a model to determine all the constant
   variables in a hierarchical model.  Basically, a constant variable in
   a particular model is any variable in the model that is defined by a
   expression of constants, or any variable that is defined by an
   expression of constants and identifiers that reference other constant
   variables.

   <p> This class computes the set of constant variables by computing the
   set of variables that are not constant and then performing the
   complement.  This is somewhat easier to compute.  The computation is
   performed in two passes, the first of which extracts the set of
   variables which must be not-constant either by not being evaluatable,
   by inclusion in an initial set, by virtue of being a PortParameter
   with an external connection, or by assignment from within a modal
   model.  The second pass collects all the variables which are not
   constant because they depend on other variables which are not
   constant.  This class also recognizes dependence declarations
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
   @since Ptolemy II 4.0
   @Pt.ProposedRating Yellow (neuendor)
   @Pt.AcceptedRating Yellow (neuendor)
*/
public class ConstVariableModelAnalysis {
    /** Analyze the given model to determine which variables must be
     *  constants and which variables may change dynamically during
     *  execution.  In addition, store the intermediate results for
     *  contained actors so they can be retrieved by the
     *  getConstVariables() method.
     *  @param model The model to be analyzed.
     *  @exception IllegalActionException If an exception occurs
     *  during analysis.
     */
    public ConstVariableModelAnalysis(Entity model)
            throws IllegalActionException {
        this(model, Collections.EMPTY_SET);
    }

    /** Analyze the given model to determine which variables must be
     *  constants and which variables may change dynamically during
     *  execution, given that all variables in the given set may
     *  change dynamically.  In addition, store the intermediate
     *  results for contained actors so they can be retrieved by the
     *  getConstVariables() method.
     *  @param model The model to be analyzed.
     *  @param variableSet The set to be analyzed.
     *  @exception IllegalActionException If an exception occurs
     *  during analysis.
     */
    public ConstVariableModelAnalysis(Entity model, Set variableSet)
            throws IllegalActionException {
        _variableToChangeContext = new HashMap();

        for (Iterator variables = variableSet.iterator(); 
            variables.hasNext();) {
            Variable variable = (Variable) variables.next();
            _variableToChangeContext.put(variable, model);
        }

        _dependencyGraph = new DirectedGraph();

        _collectConstraints(model);
        _analyzeAllVariables();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add the information in the given dependency declaration to the
     *  dependence graph of this analysis.  This method can be called
     *  by users of this class to update the analysis without
     *  recomputing all of the information from scratch.
     *  @param declaration The given dependency declaration.
     */
    public void addDependencyDeclaration(DependencyDeclaration declaration) {
        _addDependencyDeclaration(declaration);
        _analyzeAllVariables();
    }

    /** Return the analysis that is active for the given object.
     *  @param object The given object.
     *  @return The active analysis for the given object.
     *  @exception IllegalActionException If an exception occurs during 
     *  analysis.
     */
    public static ConstVariableModelAnalysis getAnalysis(NamedObj object)
            throws IllegalActionException {
        CompositeActor toplevel = (CompositeActor) object.toplevel();
        Manager manager = toplevel.getManager();

        ConstVariableModelAnalysis analysis = 
            (ConstVariableModelAnalysis) manager
            .getAnalysis("ConstVariableModelAnalysis");

        if (analysis == null) {
            analysis = new ConstVariableModelAnalysis(toplevel);
            manager.addAnalysis("ConstVariableModelAnalysis", analysis);
        }

        return analysis;
    }

    /** Return the change context of the given variable.  This an
     *  actor containing the variable, such that the variable is
     *  guaranteed not to change values during a firing of the actor.
     *  @param variable The given variable.
     *  @return The change context of the given variable.
     */
    public Entity getChangeContext(Variable variable) {
        return (Entity) _variableToChangeContext.get(variable);
    }

    /** Return the constant value of the given parameter, if the
     *  parameter is actually constant.
     *  @param variable The given variable.
     *  @return The constant value of the given variable.
     *  @exception IllegalActionException If the given parameter is
     *  not a constant parameter, as determined by this analysis.
     */
    public Token getConstantValue(Variable variable)
            throws IllegalActionException {
        if (!isConstant(variable)) {
            throw new IllegalActionException(variable,
                    "This variable does not have a constant value.");
        }

        return variable.getToken();
    }

    /** Return the computed constant variables for the given container.
     *  @param container The given container.
     *  @return The computed constant variables.
     *  @exception RuntimeException If the constant variables for the
     *  container have not already been computed.
     */
    public Set getConstVariables(NamedObj container) {
        List variables = container.attributeList(Variable.class);
        variables.removeAll(_variableToChangeContext.keySet());
        return new HashSet(variables);
    }

    /** Return the parameter dependency graph constructed through this
     *  analysis.
     *  @return The parameter dependency graph.
     */
    public DirectedGraph getDependencyGraph() {
        return _dependencyGraph;
    }

    /** Return the computed not constant variables for the given container.
     *  @param container The given container.
     *  @return The computed not constant variables.
     *  @exception RuntimeException If the constant variables for the
     *  container have not already been computed.
     */
    public Set getNotConstVariables(NamedObj container) {
        List variables = container.attributeList(Variable.class);
        variables.removeAll(getConstVariables(container));
        return new HashSet(variables);
    }

    /** Return the set of variables anywhere in the model that have
     *  the given container as least change context.
     *  @param container The given container.
     *  @return The set of variables anywhere in the model that have
     *  the given container as least change context.
     */
    public Set getVariablesWithChangeContext(NamedObj container) {
        Set variableSet = new HashSet();

        for (Iterator i = _variableToChangeContext.keySet().iterator();
             i.hasNext();) {
            Object key = i.next();
            Object value = _variableToChangeContext.get(key);

            if (value == container) {
                variableSet.add(key);
            }
        }

        return variableSet;
    }

    /** Return true if the given variable is not reconfigured in the
     *  model.  The variable is assumed to be contained by the model
     *  this analysis was created with.
     *  @param variable The given variable.
     *  @return True If the given variable is not reconfigured in the model.
     */
    public boolean isConstant(Variable variable) {
        return !_variableToChangeContext.keySet().contains(variable);
    }

    /** Return true if the variable has been analyzed by this analysis
     *  and it depends on no other parameters.
     *  @param variable The given variable.
     *  @return True If the variable has been analyzed by this analysis
     *  and it depends on no other parameters
     */
    public boolean isIndependent(Variable variable) {
        if (_dependencyGraph.backwardReachableNodes(
                _dependencyGraph.node(variable)).size() > 0) {
            return false;
        } else {
            return true;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    // Add the dependence information from the given attribute to the
    // dependence graph.
    private void _addDependencyDeclaration(DependencyDeclaration declaration) {
        Variable variable = (Variable) declaration.getContainer();
        Node targetNode = _getNode(variable);

        for (Iterator dependents = declaration.getDependents().iterator();
             dependents.hasNext();) {
            Variable dependent = (Variable) dependents.next();
            Node dependentNode = _getNode(dependent);

            //  if (!_dependencyGraph.edgeExists(node, targetNode)) {
            _dependencyGraph.addEdge(dependentNode, targetNode);

            //}
        }
    }

    // Collect all of the constraints from the given variable.
    private void _collectVariableConstraints(Variable variable) {
        Node targetNode = _getNode(variable);

        // compute the variables.
        try {
            Set freeIdentifiers = variable.getFreeIdentifiers();

            for (Iterator names = freeIdentifiers.iterator(); 
                names.hasNext();) {
                String name = (String) names.next();
                Variable dependent = ModelScope.getScopedVariable(variable,
                        variable, name);

                if (dependent != null) {
                    Node dependentNode = _getNode(dependent);
                    _dependencyGraph.addEdge(dependentNode, targetNode);
                }
            }
        } catch (IllegalActionException ex) {
            // Assume that this will be changed later...
            // i.e. input_isPresent in FSM.
            // Note that this also traps expressions that
            // have no value as being variable...
            _updateChangeContext(variable, (Entity) variable.toplevel());
        }
    }

    // Collect the set of variables in the given entity which might
    // change during execution.  This method adds an entry in the
    // given map from each dynamic parameter deeply contained in the
    // given entity to the change context of that parameter.
    private void _collectConstraints(NamedObj container)
            throws IllegalActionException {
        if (container instanceof Variable) {
            Variable variable = (Variable) container;
            _collectVariableConstraints(variable);
        }

        if (container instanceof DependencyDeclaration) {
            DependencyDeclaration declaration = 
                (DependencyDeclaration) container;
            _addDependencyDeclaration(declaration);
        }

        if (container instanceof PortParameter) {
            PortParameter parameter = (PortParameter) container;
            ParameterPort port = parameter.getPort();

            // Under what conditions is a PortParameter not associated
            // with a port?
            if ((port != null) && (port.getWidth() > 0)) {
                _updateChangeContext(parameter,
                        (Entity) parameter.getContainer());
            }
        }

        if (container instanceof ExplicitChangeContext) {
            List list = ((ExplicitChangeContext) container)
                .getModifiedVariables();

            for (Iterator variables = list.iterator(); variables.hasNext();) {
                Variable variable = (Variable) variables.next();
                _updateChangeContext(variable,
                        ((ExplicitChangeContext) container).getContext());
            }
        }

        // Recurse through the whole model.
        for (Iterator attributes = container.attributeList().iterator();
             attributes.hasNext();) {
            Attribute attribute = (Attribute) attributes.next();
            _collectConstraints(attribute);
        }

        if (container instanceof CompositeEntity) {
            CompositeEntity composite = (CompositeEntity) container;

            for (Iterator entities = composite.entityList().iterator();
                 entities.hasNext();) {
                _collectConstraints((Entity) entities.next());
            }
        }

        if (container instanceof Entity) {
            for (Iterator ports = ((Entity) container).portList().iterator();
                 ports.hasNext();) {
                Port port = (Port) ports.next();
                _collectConstraints(port);
            }
        }
    }

    // Recursively compute the set of constant variables for all actors
    // deeply contained in the given model.
    private void _analyzeAllVariables() {
        // Sets of variables used to track the fixed point iteration.
        LinkedList workList = new LinkedList(_variableToChangeContext.keySet());

        while (!workList.isEmpty()) {
            Variable variable = (Variable) workList.removeFirst();
            Node node = _dependencyGraph.node(variable);
            Entity changeContext = (Entity) _variableToChangeContext.get(variable);

            for (Iterator outputEdges = _dependencyGraph.outputEdges(node)
                     .iterator();
                 outputEdges.hasNext();) {
                Node sinkNode = ((Edge) outputEdges.next()).sink();
                Variable targetVariable = (Variable) sinkNode.getWeight();

                if (_updateChangeContext(targetVariable, changeContext)
                        && !workList.contains(targetVariable)) {
                    workList.addLast(targetVariable);
                }
            }
        }
    }

    // Get the node for the given variable, adding it to the graph if
    // necessary.
    private Node _getNode(Variable variable) {
        if (_dependencyGraph.containsNodeWeight(variable)) {
            return _dependencyGraph.node(variable);
        } else {
            return _dependencyGraph.addNodeWeight(variable);
        }
    }

    // Update the change context associated with the given variable to
    // be at least the given change context.
    // return true if a change occurred
    private final boolean _updateChangeContext(Variable variable,
            Entity changeContext) {
        Entity oldChangeContext = (Entity) _variableToChangeContext.get(variable);

        //         System.out.println("variable = " + variable);
        //         System.out.println("oldChangeContext = " + oldChangeContext);
        //         System.out.println("undatedChangeContext = " + changeContext);
        Entity newChangeContext = _computeBound(changeContext, oldChangeContext);

        //         System.out.println("newChangeContext = " + newChangeContext);
        if (newChangeContext != oldChangeContext) {
            if (newChangeContext != null) {
                _variableToChangeContext.put(variable, newChangeContext);
            }

            return true;
        }

        return false;
    }

    // Return the entity which is contained by the other in the ptolemy
    // hierarchy.  If neither contains the other, then throw an
    // exception.  If entity2 is null (corresponding to a static
    // change context), then return entity1.
    private final Entity _computeBound(Entity entity1, Entity entity2) {
        if ((entity2 == null) || entity2.equals(entity1)) {
            return entity1;
        }

        if (entity2.deepContains(entity1)) {
            return entity1;
        } else if (entity1.deepContains(entity2)) {
            return entity2;
        } else {
            throw new RuntimeException("Illegal change context");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private static ConstVariableModelAnalysis _previousCache;
    private static Entity _previousCacheToplevel;
    private DirectedGraph _dependencyGraph;
    private Map _variableToChangeContext;
    private CompositeActor _model;
}
