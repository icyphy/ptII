/* A Scheduler for the PSDF domain

 Copyright (c) 1998-2003 The Regents of the University of California.
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

@ProposedRating Red (neuendor@eecs.berkeley.edu)
@AcceptedRating Red (neuendor@eecs.berkeley.edu)
*/

package ptolemy.domains.psdf.kernel;

import java.util.*;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.IORelation;
import ptolemy.actor.Receiver;
import ptolemy.actor.parameters.ParameterPort;
import ptolemy.actor.sched.Firing;
import ptolemy.actor.sched.NotSchedulableException;
import ptolemy.actor.sched.Schedule;
import ptolemy.actor.sched.ScheduleElement;
import ptolemy.actor.sched.Scheduler;
import ptolemy.actor.sched.StaticSchedulingDirector;
import ptolemy.actor.util.ConstVariableModelAnalysis;
import ptolemy.actor.util.DependencyDeclaration;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.*;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.ComponentPort;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.StreamListener;
import ptolemy.kernel.util.ValueListener;
import ptolemy.kernel.util.Workspace;
import ptolemy.math.Fraction;

// FIXME: fixup import lists.
import synthesis.dif.psdf.*;
import ptolemy.graph.*;

///////////////////////////////////////////////////////////
//// PSDFScheduler
/**

A scheduler that implements basic scheduling of PSDF graphs.  PSDF
scheduling is similar to SDF scheduling, EXCEPT: 

<p> 1) Because parameter values may change, the solution to the
balance equation is computed symbolically.  i.e. the repetitions
vector is a function of the parameter values. 

<p> 2) Because the firing vector may change, the schedule determined
by this class can only be a quasi-static, or parameterized schedule.
Note that parameterized schedules cannot generally be constructed for
models with feedback or with unconstrained parameter values.

<p> This class uses a ConstVariableModelAnalysis to determine which
scheduling parameters are constants and which may change during
execution of the model.  Rate parameters that can change are checked
to ensure that their change context is not strictly contained by the
model being scheduled.  If this is the case, then the actor is not
locally synchronous, and cannot be statically scheduled.  Dynamic
parameters with a valid changed context are treated symbolically when
computing the repetitions vector.

<p> After computing a schedule, this scheduler determines the external
rate of each of the model's external ports.  Since the firing vector
is only computed symbolically, these rates can also only be computed
symbolically.  The dependence of these external rates on the rates of
ports in the model is declared using a DependenceDeclaration.  Higher
level directors may use this dependence information to determine the
change context of those rate variables and may refuse to schedule the
composite actor if those rates imply that this model is not locally
synchronous.

<p> This scheduler uses a version of the P-APGAN scheduling algorithm 
described in [1].

<p> [1] B. Bhattacharya and S. S. Bhattacharyya. Quasi-static scheduling of
reconfigurable dataflow graphs for DSP systems. In <em> Proceedings of the
International Workshop on Rapid System Prototyping </em>, 
pages 84-89, Paris, France, June 2000.

@see ptolemy.actor.sched.Scheduler
@see ptolemy.domains.sdf.lib.SampleDelay
@see ptolemy.domains.sdf.kernel.SDFScheduler

@author Stephen Neuendorffer, Shuvra S. Bhattacharyya
@version $Id$
@since Ptolemy II 0.2
*/
public class PSDFScheduler extends ptolemy.domains.sdf.kernel.SDFScheduler {
    /** Construct a scheduler with no container(director)
     *  in the default workspace, the name of the scheduler is
     *  "Scheduler".
     */
    public PSDFScheduler() 
            throws IllegalActionException, NameDuplicationException {
        super();
        _init();
    }

    /** Construct a scheduler in the given workspace with the name
     *  "Scheduler".
     *  If the workspace argument is null, use the default workspace.
     *  The scheduler is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *
     *  @param workspace Object for synchronization and version tracking.
     */
    public PSDFScheduler(Workspace workspace) 
            throws IllegalActionException, NameDuplicationException {
        super(workspace);
        _init();
    }

    /** Construct a scheduler in the given container with the given name.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.  This attribute will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public PSDFScheduler(Director container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _init();
    }

    /** Return the parameterized scheduling sequence.  
     *  An exception will be thrown if the graph is not schedulable.  
     *
     *  @return A schedule of the deeply contained opaque entities
     *  in the firing order.
     *  @exception NotSchedulableException If a parameterized schedule 
     *  cannot be derived for the model.
     *  @exception IllegalActionException If the rate parameters
     *  of the model are not correct, or the computed rates for
     *  external ports are not correct.
     */

    protected Schedule _getSchedule() 
            throws NotSchedulableException, IllegalActionException {
        _debug("Starting PSDFScheduler._getSchedule()\n"); 
        PSDFDirector director = (PSDFDirector)getContainer();
        CompositeActor model = (CompositeActor)director.getContainer();
        PSDFGraphReader graphReader = new PSDFGraphReader();
        PSDFGraph psdfGraph = (PSDFGraph) (graphReader.convert(model));
        _debug("Finished converting to a PSDF graph\n"); 
        _debug(psdfGraph.toString() + "\n"); 
        psdfGraph.printEdgeRateExpressions();
        _debug("Invoking the P-APGAN algorithm\n"); 
        PSDFAPGANStrategy scheduler = new PSDFAPGANStrategy(psdfGraph);
        ptolemy.graph.sched.Schedule schedule = scheduler.schedule();
        _debug("Returned from P-APGAN; the schedule follows.\n"); 
        _debug(schedule.toString() + "\n"); 

        SymbolicScheduleElement result = 
                 _expandAPGAN(psdfGraph, scheduler.getClusteredGraphRoot(), 
                 scheduler);
        _debug("Completed PSDFScheduler._getSchedule().\n The "
                + "schedule follows.\n" + result.toString() + "\n");

        return (Schedule)result;
    }

    // Evaluate the given parse tree in the scope of the the model
    // being scheduled, resolving "::" scoping syntax inside the
    // model.
    private Token _evaluateExpressionInModelScope(ASTPtRootNode node)
            throws IllegalActionException {
        if (_parseTreeEvaluator == null) {
            _parseTreeEvaluator = new ParseTreeEvaluator();
        }      
        if (_parserScope == null) {
            _parserScope = new ScheduleScope();
        } 
        Token result = _parseTreeEvaluator.evaluateParseTree(
                node, _parserScope);
        return result;
    }

    // Expand the P-APGAN-clustered graph. The schedule element that is
    // returned has an iteration count of 1. This iteration count expression
    // can be changed by the caller to iterate the schedule computed in
    // this method.
    // @param graph The graph containing the node.
    // @param node The super node to expand.
    // @param apgan The scheduler that was used to build the cluster hierarchy.
    // @return The schedule saving the expansion result.
    private SymbolicScheduleElement _expandAPGAN(PSDFGraph graph, 
            ptolemy.graph.Node node, PSDFAPGANStrategy apgan) {
        PSDFGraph childGraph = (PSDFGraph)apgan.getSubgraph(node);

        try {
            // Atomic node
            if (childGraph == null) {
                PSDFNodeWeight weight = (PSDFNodeWeight)node.getWeight();
                SymbolicFiring firing = new SymbolicFiring((Actor)
                        weight.getComputation(), "1");
                return firing;
            // Super node
            } else {
                Schedule schedule = new Schedule();

                // Expand the super node with adjacent nodes contained 
                // within it.
                Edge edge = (Edge)childGraph.edges().iterator().next();
                ptolemy.graph.Node source = edge.source();
                ptolemy.graph.Node sink   = edge.sink();
                SymbolicScheduleElement first  = 
                        _expandAPGAN(childGraph, source, apgan);
                SymbolicScheduleElement second = 
                        _expandAPGAN(childGraph, sink, apgan);

                // Determine the iteration counts of the source and
                // sink clusters.
                String producedExpression = apgan.producedExpression(edge);
                String consumedExpression = apgan.consumedExpression(edge);

                // These errors should not occur.
                if (producedExpression == null) {
                    throw new RuntimeException("Internal error: null "
                            + "production rate expression. The offending edge "
                            + "follows.\n" + edge);
                } else if (consumedExpression == null) {
                    throw new RuntimeException("Internal error: null "
                            + "consumption rate expression. The offending edge "
                            + "follows.\n" + edge);
                }

                String denominator = PSDFGraphs.gcdExpression(
                        producedExpression, consumedExpression);
                String firstIterations = "(" + consumedExpression + ") / (" 
                        + denominator + ")";
                String secondIterations = "(" + producedExpression + ") / ("
                        + denominator + ")";

                first.setIterationCount(firstIterations);
                second.setIterationCount(secondIterations);

                SymbolicSchedule symbolicSchedule = new SymbolicSchedule("1");
                symbolicSchedule.add((ScheduleElement)first);
                symbolicSchedule.add((ScheduleElement)second);

                // Compute buffer sizes and associate them with the 
                // corresponding relations.
                Iterator edges = childGraph.edges().iterator();
                while (edges.hasNext()) {
                    Edge nextEdge = (Edge)edges.next();
                    PSDFEdgeWeight weight = 
                            (PSDFEdgeWeight)nextEdge.getWeight();
                    IOPort sourcePort = weight.getSourcePort();
                    List relationList = sourcePort.linkedRelationList();
                    if (relationList.size() != 1) {
                        // FIXME: Need to generalize this?
                        throw new RuntimeException("Cannot handle relation "
                                + "lists that are not singletons.\nThe size of "
                                + "this relation list is " + relationList.size()
                                + "\nA dump of the offending edge follows.\n"
                                + nextEdge + "\n");
                    }
                    Iterator relations = relationList.iterator();
                    Relation relation = (Relation)relations.next();
                    String produced = apgan.producedExpression(nextEdge);
                    String consumed = apgan.consumedExpression(nextEdge);
                    String bufferSizeExpression = "((" 
                            + produced 
                            + ") * ("
                            + consumed
                            + ")) / "
                            + PSDFGraphs.gcdExpression(produced, consumed);

                    // Due to the bottom-up traversal in _expandAPGAN, relations
                    // that are linked to multiple sink nodes will 
                    // have their buffer sizes progressively replaced by
                    // those of outer clusterings, and will end up with
                    // the buffer size determined by the outermost clustering.
                    _bufferSizeMap.put(relation, bufferSizeExpression); 
                }
                

                return symbolicSchedule;
            }
        } catch (Exception exception) {
            throw new RuntimeException("Error converting cluster hierarchy to "
                    + "schedule.\n" + exception.getMessage());
        }
    }

    // Initialize the object.
    private void _init() {
        if (_debugFlag) {
            addDebugListener(new StreamListener());
        }
        _bufferSizeMap = new HashMap();
    }

    /** An actor firing with an iteration count that is determined by
     *  a symbolic expression.
     */
    private class SymbolicFiring extends Firing implements 
            SymbolicScheduleElement {
        /** Construct a firing with the given actor and the given
         *  expression.  The given actor
         *  is assumed to fire the number of times determined by
         *  evaluating the given expression.  
         *  @param actor The actor in the firing.
         *  @param expression The expression associated with the firing.
         */
        public SymbolicFiring(Actor actor, String expression)
                throws IllegalActionException {
            super(actor); 
            setIterationCount(expression);
        }

        /** Return the most recent expression that was used to set the
         *  iteration count of this symbolic firing.
         *  @return The most recent expression.
         *  @see setIterationCount(String).
         */
        public String expression() {
            return _expression;
        }

        /** Return the current iteration count of this firing.
         */
        public int getIterationCount() {
            try {
                IntToken token = (IntToken)
                        _evaluateExpressionInModelScope(_parseTree);
                System.out.println("firing " + getActor() + " " +
                        token.intValue() + " times");
                return token.intValue();
            } catch (Exception ex) {
                // FIXME: this isn't very nice.
                throw new RuntimeException(
                        "Error evaluating parse tree for expression"
                        + ": " + expression(), ex);
            }
        }

        /** Get the parse tree of the iteration expression. 
         *  @return The parse tree.
         */
        public ASTPtRootNode parseTree() {
            return _parseTree;
        }

        /** Set the expression associated with the iteration count.
         *  The expression will probably be something like
         *  "a2::in::tokenConsumptionRate/gcd(a2::in::tokenConsumptionRate,
         *  a::out::tokenProductionRate)."
         *
         *  @param expression The expression to be associated with the iteration
         *  count.
         */
        public void setIterationCount(String expression) {
            _expression = expression;
            // FIXME: Need better exception handling.
            try {
                PtParser parser = new PtParser();
                _parseTree = parser.generateParseTree(expression);
            } catch (Exception exception) {
                throw new RuntimeException("Error setting iteration count to "
                        + expression + ".\n" + exception.getMessage());
            }
        }

        /**
         * Output a string representation of this symbolic firing.
         */
        public String toString() {
            String result = "Fire Actor " + getActor().toString();
            result += "[" +  expression() + "] times";
            return result;
        }

        // The iteration expression. This is stored separately for
        // diagnostic purposes only.
        private String _expression;

        // The parse tree of the iteration expression.
        private ASTPtRootNode _parseTree;

    }

    /** A schedule whose iteration count is given by an expression.
     */
    private class SymbolicSchedule extends Schedule  implements 
            SymbolicScheduleElement {
        /** Construct a symbolic schedule with the given expression.
         *  This schedule is assumed to fire the number of times determined
         *  by evaluating the given expression.
         *  @param expression The expression associated with the schedule. 
         */
        public SymbolicSchedule(String expression)
                throws IllegalActionException {
            setIterationCount(expression);
        }

        /** Return the most recent expression that was used to set the
         *  iteration count of this symbolic firing.
         *  @return The most recent expression.
         *  @see setIterationCount(String).
         */
        public String expression() {
            return _expression;
        }

        /** Return the current iteration count of this symbolic schedule.
         */
        public int getIterationCount() {
            try {
                IntToken token = (IntToken)
                        _evaluateExpressionInModelScope(_parseTree);
                return token.intValue();
            } catch (Exception ex) {
                // FIXME: this isn't very nice.
                throw new RuntimeException(ex.getMessage());
            }
        }

        /** Get the parse tree of the iteration expression. 
         *  @return The parse tree.
         */
        public ASTPtRootNode parseTree() {
            return _parseTree;
        }

        /** Set the expression associated with the iteration count.
         *  The expression will probably be something like
         *  "a2::in::tokenConsumptionRate/gcd(a2::in::tokenConsumptionRate,
         *  a::out::tokenProductionRate)."
         *
         *  @param expression The expression to be associated with the iteration
         *  count.
         */
        public void setIterationCount(String expression) {
            _expression = expression;
            // FIXME: Need better exception handling.
            try {
                PtParser parser = new PtParser();
                _parseTree = parser.generateParseTree(expression);
            } catch (Exception exception) {
                throw new RuntimeException("Error setting iteration count to "
                        + expression + ".\n" + exception.getMessage());
            }
        }

        /** Return a string representation of this symbolic schedule.
         *  @return The string representation.
         */
        public String toString() {
            String result = "Execute Symbolic Schedule{\n";
            Iterator elements = iterator();
            while (elements.hasNext()) {
                ScheduleElement element = (ScheduleElement)elements.next();
                result += element + "\n";
            }
            result += "}";
            result += "[" + expression() + "] times"; 
            return result; 
        }

        // The iteration expression. This is stored separately for
        // diagnostic purposes only.
        private String _expression;

        // The parse tree of the iteration expression.
        private ASTPtRootNode _parseTree;
    }

    /** An interface for schedule elements whose iteration counts are
     *  in terms of symbolic expressions.
     */ 
    private interface SymbolicScheduleElement {
        // FIXME: populate with more methods as appropriate.

        /** Get the parse tree of the iteration expression. 
         *  @return The parse tree.
         */
        public ASTPtRootNode parseTree();

        /** Set the expression associated with the iteration count.
         *  The expression will probably be something like
         *  "a2::in::tokenConsumptionRate/gcd(a2::in::tokenConsumptionRate,
         *  a::out::tokenProductionRate)."
         *
         *  @param expression The expression to be associated with the iteration
         *  count.
         */
        public void setIterationCount(String expression);
    }

    /** Scope implementation with local caching. */
    private class ScheduleScope extends ModelScope {
        
        /** Construct a scope consisting of the variables
         *  of the container of the the enclosing instance of
         *  Variable and its containers and their scope-extending
         *  attributes.
         */
        public ScheduleScope() {
        }

        /** Look up and return the attribute with the specified name in the
         *  scope. Return null if such an attribute does not exist.
         *  @return The attribute with the specified name in the scope.
         *  @exception IllegalActionException If a value in the scope
         *  exists with the given name, but cannot be evaluated.
         */
        public ptolemy.data.Token get(String name)
                throws IllegalActionException {
            PSDFDirector director = (PSDFDirector)getContainer();
            CompositeActor reference = (CompositeActor)director.getContainer();
            Variable result;
            if(name.indexOf("::") != -1) {
                String insideName = name.replaceAll("::", ".");
                System.out.println("insideName = " + insideName);
                result = (Variable)reference.getAttribute(insideName);
            } else {
                result = getScopedVariable(
                        null,
                        reference,
                        name);
            }
            
            if (result != null) {
                return result.getToken();
            } else {
                return null;
            }
        }

        /** Look up and return the type of the attribute with the
         *  specified name in the scope. Return null if such an
         *  attribute does not exist.
         *  @return The attribute with the specified name in the scope.
         *  @exception IllegalActionException If a value in the scope
         *  exists with the given name, but cannot be evaluated.
         */
        public ptolemy.data.type.Type getType(String name)
                throws IllegalActionException {
            PSDFDirector director = (PSDFDirector)getContainer();
            CompositeActor reference = (CompositeActor)director.getContainer();
            Variable result;
            if(name.indexOf("::") != -1) {
                String insideName = name.replaceAll("::", ".");
                result = (Variable)reference.getAttribute(insideName);
            } else {
                result = getScopedVariable(
                        null,
                        reference,
                        name);
            }

            if (result != null) {
                return result.getType();
            } else {
                return null;
            }
        }

        /** Return the list of identifiers within the scope.
         *  @return The list of variable names within the scope.
         */
        public Set identifierSet() {
            NamedObj reference = (CompositeActor)
                PSDFScheduler.this.getContainer();
            return getAllScopedVariableNames(null, reference);
        }
    }

    // A map from relations into expressions that give symbolic buffer sizes 
    // of the relations. Keys are of type Relation and values are of type
    // String.
    private HashMap _bufferSizeMap;

    private boolean _debugFlag = true;
    private ParseTreeEvaluator _parseTreeEvaluator;
    private ParserScope _parserScope;
}
