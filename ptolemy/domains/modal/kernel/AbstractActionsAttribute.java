/* A base class for actions with semicolon delimited lists of commands.

 Copyright (c) 2000-2014 The Regents of the University of California.
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
package ptolemy.domains.modal.kernel;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.ASTPtAssignmentNode;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.ParseTreeEvaluator;
import ptolemy.data.expr.ParseTreeFreeVariableCollector;
import ptolemy.data.expr.ParseTreeTypeInference;
import ptolemy.data.expr.ParseTreeWriter;
import ptolemy.data.expr.ParserScope;
import ptolemy.data.expr.PtParser;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.HasTypeConstraints;
import ptolemy.data.type.MonotonicFunction;
import ptolemy.data.type.Type;
import ptolemy.data.type.Typeable;
import ptolemy.graph.Inequality;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// AbstractActionsAttribute

/**
 A base class for actions with semicolon delimited lists of commands.
 <p>
 The value of this attribute is a semicolon separated list of commands,
 where each command gives a destination to send data to and a value
 to send. The actions are given by calling setExpression() with
 a string of the form:
 <pre>
 <i>command</i>; <i>command</i>; ...
 </pre>
 where each <i>command</i> has the form:
 <pre>
 <i>destination</i> = <i>expression</i>
 </pre>
 where <i>destination</i> is either
 <pre>
 <i>name</i>
 </pre>
 or
 <pre>
 <i>name</i>(<i>number</i>)
 </pre>
 <p>
 The <i>expression</i> is a string giving an expression in the usual
 Ptolemy II expression language.  The expression may include references
 to variables and parameters contained by the FSM actor.

 @author Xiaojun Liu and Edward A. Lee
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (eal)
 @see CommitActionsAttribute
 @see Transition
 @see FSMActor
 */
public abstract class AbstractActionsAttribute extends Action implements
HasTypeConstraints {
    /** Construct an action with the given name contained
     *  by the specified container (which should be a Transition when used in
     *  the FSM domain, and an Event in the Ptera domain). The <i>container</i>
     *  argument must not
     *  be null, or a NullPointerException will be thrown. This action will
     *  use the workspace of the container for synchronization and
     *  version counts. If the name argument is null, then the name is
     *  set to the empty string.
     *  This increments the version of the workspace.
     *  @param container The container that contains this action.
     *  @param name The name of this action.
     *  @exception IllegalActionException If the action is not of an
     *   acceptable class for the container, or if the name contains
     *   a period.
     *  @exception NameDuplicationException If the container already
     *   has an attribute with the name.
     */
    public AbstractActionsAttribute(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    /** Construct an action in the specified workspace with an empty
     *  string as a name.
     *  The object is added to the directory of the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the attribute.
     */
    public AbstractActionsAttribute(Workspace workspace) {
        super(workspace);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets the attribute public members to refer
     *  to the attributes of the new actor.
     *  @param workspace The workspace for the new actor.
     *  @return A new FSMActor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        AbstractActionsAttribute newObject = (AbstractActionsAttribute) super
                .clone(workspace);
        newObject._destinations = null;
        newObject._destinationsListVersion = -1;
        newObject._numbers = null;
        newObject._parseTreeEvaluator = null;
        newObject._parseTrees = null;

        newObject._scope = null;

        // The _destinationNames is a list of ports or parameter names that are
        // written to by this action. It is constructed in setExpression().
        // The clone needs to reconstruct this.
        newObject._destinationNames = null;
        try {
            newObject.setExpression(getExpression());
        } catch (IllegalActionException e) {
            throw new CloneNotSupportedException(e.getMessage());
        }

        return newObject;
    }

    /** Execute this action.  For each destination identified in the
     *  action, compute the value in the action and perform the
     *  particular assignment.  This method should be extended by
     *  derived classes to perform the evaluation and assignment as
     *  appropriate.
     *  @exception IllegalActionException If a destination is not found.
     */
    @Override
    public void execute() throws IllegalActionException {
        if (_destinationsListVersion != workspace().getVersion()) {
            _updateDestinations();
        }

        if (_parseTreeEvaluator == null) {
            _parseTreeEvaluator = new ParseTreeEvaluator();
        }
    }

    /** Return the list of channel numbers given in expression set
     *  for this attribute.  If no destinations are specified, then return
     *  an empty list.
     *  @return the list of channel numbers.
     *  @exception IllegalActionException
     */
    public List getChannelNumberList() throws IllegalActionException {
        List list = new LinkedList();
        if (_numbers != null) {
            Iterator iterator = _numbers.iterator();
            while (iterator.hasNext()) {
                Object next = iterator.next();
                if (next instanceof Integer) {
                    list.add(next);
                } else if (next instanceof ASTPtRootNode) {
                    Token token = _parseTreeEvaluator.evaluateParseTree(
                            (ASTPtRootNode) next, _getParserScope());
                    list.add(((IntToken) token).intValue());
                } else {
                    list.add(null);
                }
            }
        }
        return list;
    }

    /** Return the destination object referred to by the given name.
     *  Depending on the subclass of this class, this might be a variable,
     *  or an output port.
     *  @param name The name of the destination object.
     *  @return The destination object with the given name.
     *  @exception IllegalActionException If the given name is not a valid
     *  destination for this action.
     */
    public NamedObj getDestination(String name) throws IllegalActionException {
        return _getDestination(name);
    }

    /** Return the list of destinations of assignments in this action.
     *  @return A list of IOPort for output actions, and a list of parameters
     *   for set actions.
     *  @exception IllegalActionException If the destination list cannot be
     *   constructed.
     */
    @Override
    public List getDestinations() throws IllegalActionException {
        if (_destinationsListVersion != workspace().getVersion()) {
            _updateDestinations();
        }
        return _destinations;
    }

    /** Return the list of destination names given in expression set
     *  for this attribute.  If no destinations are specified, then return
     *  an empty list.
     *  @return the list of destination names.
     */
    public List getDestinationNameList() {
        if (_destinationNames == null) {
            return new LinkedList();
        } else {
            return Collections.unmodifiableList(_destinationNames);
        }
    }

    /** Return the expression referred to by the given name.  When the
     *  action is executed, this expression will be evaluated and
     *  assigned to the object associated with the name.
     *  @param name The name of an expression.
     *  @return The expression referred to by the given name.
     *  @see #setExpression
     */
    public String getExpression(String name) {
        ParseTreeWriter writer = new ParseTreeWriter();
        return writer.printParseTree((ASTPtRootNode) _parseTrees
                .get(_destinationNames.indexOf(name)));
    }

    /** Return the parse tree referred to by the given name.
     *  @param name The name of a parse tree.
     *  @return The parse tree referred to by the given name.
     */
    public ASTPtRootNode getParseTree(String name) {
        return (ASTPtRootNode) _parseTrees.get(_destinationNames.indexOf(name));
    }

    /** Return the list of parse trees given in expression set
     *  for this attribute.  If no destinations are specified, then return
     *  an empty list.
     *  @return the list of parse trees.
     */
    public List getParseTreeList() {
        if (_parseTrees == null) {
            return new LinkedList();
        } else {
            return Collections.unmodifiableList(_parseTrees);
        }
    }

    /** Test if a channel number is associated with the given name.
     *  @param name The channel name.
     *  @return true If a channel was specified.
     */
    public boolean isChannelSpecified(String name) {
        Integer integer = (Integer) _numbers.get(_destinationNames
                .indexOf(name));
        return integer != null;
    }

    /** Set the action and notify the container
     *  that the action has changed by calling attributeChanged(),
     *  and notify any listeners that have
     *  been registered using addValueListener().
     *  @param expression The action.
     *  @exception IllegalActionException If the change is not acceptable
     *  to the container, or if the action is syntactically incorrect.
     *  @see #getExpression
     */
    @Override
    public void setExpression(String expression) throws IllegalActionException {
        super.setExpression(expression);

        // Initialize the lists that store the commands to be executed.
        // NOTE: This must be done before we return if the expression is
        // null, otherwise, previous set actions will continue to be
        // executed.
        _destinationNames = new LinkedList();
        _numbers = new LinkedList();
        _parseTrees = new LinkedList();

        // Indicate that the _destinations list is invalid.  We defer
        // determination of the destinations because the destinations
        // may not have been created yet.
        _destinationsListVersion = -1;

        // This is important for InterfaceAutomata which extend from
        // this class.
        if (expression == null || expression.trim().equals("")) {
            return;
        }

        PtParser parser = new PtParser();
        Map map = parser.generateAssignmentMap(expression);

        for (Iterator names = map.entrySet().iterator(); names.hasNext();) {
            Map.Entry entry = (Map.Entry) names.next();
            ASTPtAssignmentNode node = (ASTPtAssignmentNode) entry.getValue();

            // Parse the destination specification first.
            String completeDestinationSpec = node.getIdentifier();
            int openParen = completeDestinationSpec.indexOf("(");

            if (openParen > 0) {
                // A channel is being specified.
                int closeParen = completeDestinationSpec.indexOf(")");

                if (closeParen < openParen) {
                    throw new IllegalActionException(this,
                            "Malformed action: expected destination = "
                                    + "expression. Got: "
                                    + completeDestinationSpec);
                }

                _destinationNames.add(completeDestinationSpec.substring(0,
                        openParen).trim());

                String channelSpec = completeDestinationSpec.substring(
                        openParen + 1, closeParen);

                try {
                    _numbers.add(Integer.valueOf(channelSpec));
                } catch (NumberFormatException ex) {
                    _numbers.add(parser.generateParseTree(channelSpec));
                }
            } else {
                // No channel is specified.
                _destinationNames.add(completeDestinationSpec);
                _numbers.add(null);
            }

            // Parse the expression
            _parseTrees.add(node.getExpressionTree());
        }
    }

    /** Give a descriptive string.
     *  @return The expression.
     */
    @Override
    public String toString() {
        return getExpression();
    }

    /** Return the type constraints of this object.
     *  The constraints are a set of inequalities.
     *  @return a list of instances of Inequality.
     *  @see ptolemy.graph.Inequality
     */
    @Override
    public Set<Inequality> typeConstraints() {
        Set<Inequality> list = new HashSet<Inequality>();

        for (Iterator names = getDestinationNameList().iterator(); names
                .hasNext();) {
            String name = (String) names.next();

            try {
                NamedObj object = getDestination(name);

                if (object instanceof Typeable) {
                    InequalityTerm term = ((Typeable) object).getTypeTerm();
                    list.add(new Inequality(new TypeFunction(name), term));
                }
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }

        return list;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Given a destination name, return a NamedObj that matches that
     *  destination.  An implementation of this method should never return
     *  null (throw an exception instead).
     *  @param name The name of the destination, or null if none is found.
     *  @return An object (like a port or a variable) with the specified name.
     *  @exception IllegalActionException If the associated FSMActor
     *   does not have a destination with the specified name.
     */
    protected abstract NamedObj _getDestination(String name)
            throws IllegalActionException;

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** Return a parser scope used to evaluate or type-check this action.
     *
     *  @return The parser scope.
     */
    protected ParserScope _getParserScope() {
        if (_scope == null) {
            FSMActor fsmActor = (FSMActor) getContainer().getContainer();
            _scope = fsmActor.getPortScope();
        }
        return _scope;
    }

    /** List of destination names. */
    protected List _destinationNames;

    /** List of destinations. */
    protected List _destinations;

    /** The workspace version number when the _destinations list is last
     *  updated.
     */
    protected long _destinationsListVersion = -1;

    /** The parse tree evaluator. */
    protected ParseTreeEvaluator _parseTreeEvaluator;

    /** The list of parse trees. */
    protected List _parseTrees;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /*  Cache a reference to each destination of this action.  For
     *  each destination in the _destinationNames list, create a
     *  corresponding entry in the _destinations list that refers to
     *  the destination.
     *  @exception IllegalActionException If the associated FSMActor
     *   does not have a destination with the specified name.
     */
    private void _updateDestinations() throws IllegalActionException {
        try {
            workspace().getReadAccess();
            _destinations = new LinkedList();

            if (_destinationNames != null) {
                Iterator destinationNames = _destinationNames.iterator();
                while (destinationNames.hasNext()) {
                    String destinationName = (String) destinationNames.next();
                    NamedObj destination = _getDestination(destinationName);
                    _destinations.add(destination);
                }
            }

            _destinationsListVersion = workspace().getVersion();
        } finally {
            workspace().doneReading();
        }
    }

    /** The scope. */
    private ParserScope _scope;

    // This class implements a monotonic function of the type of
    // the output port.
    // The function value is determined by type inference on the
    // expression, in the scope of this Expression actor.
    private class TypeFunction extends MonotonicFunction {
        /** Create a new type function.  This function represents a
         * constraint on the type of the destination with the given
         * name.
         */
        public TypeFunction(String name) {
            _name = name;
        }

        ///////////////////////////////////////////////////////////////
        ////                       public inner methods            ////

        /** Return the function result.
         *  @return A Type.
         *  @exception IllegalActionException If inferring types for the
         *  expression fails.
         */
        @Override
        public Object getValue() throws IllegalActionException {
            try {
                // Deal with the singularity at UNKNOWN..  Assume that if
                // any variable that the expression depends on is UNKNOWN,
                // then the type of the whole expression is unknown..
                // This allows us to properly find functions that do exist
                // (but not for UNKNOWN arguments), and to give good error
                // messages when functions are not found.
                InequalityTerm[] terms = getVariables();

                for (InequalityTerm term : terms) {
                    if (term != this && term.getValue() == BaseType.UNKNOWN) {
                        return BaseType.UNKNOWN;
                    }
                }

                int index = _destinationNames.indexOf(_name);
                ASTPtRootNode parseTree = (ASTPtRootNode) _parseTrees
                        .get(index);

                Type type = _typeInference.inferTypes(parseTree,
                        _getParserScope());

                // Return the array type with type as the element type when
                // there is an index following the name and the name resolves to
                // a variable. E.g., A(i) accesses the i-th element of variable
                // A, so when we get "type" as the type of A(i), we return the
                // array type constructed with "type" as the type of A itself.
                // -- tfeng (11/22/2008)
                NamedObj container = getContainer();
                while (container != null && !(container instanceof Entity)) {
                    container = container.getContainer();
                }
                if (container != null
                        && ((Entity) container).getPort(_name) == null) {
                    // Not a port, then it must be a variable.
                    if (_numbers.get(index) != null &&
                            // If the destination is not a variable, it should
                            // be a port, and port(i) refers to the i-th channel
                            // of the port, which has the same type as the port
                            // itself.
                            // -- tfeng (11/26/2008)
                            getDestination(_name) instanceof Variable) {
                        // Has a number in parentheses following the name.
                        ArrayType arrayType = new ArrayType(type);
                        return arrayType;
                    }
                }

                return type;
            } catch (Exception ex) {
                throw new IllegalActionException(AbstractActionsAttribute.this,
                        ex,
                        "An error occurred during expression type inference");
            }
        }

        /** Return the type variable in this inequality term. If the type
         *  of input ports are not declared, return an one element array
         *  containing the inequality term representing the type of the port;
         *  otherwise, return an empty array.
         *  @return An array of InequalityTerm.
         */
        @Override
        public InequalityTerm[] getVariables() {
            // Return an array that contains type terms for all of the
            // inputs and all of the parameters that are free variables for
            // the expression.
            try {
                ASTPtRootNode parseTree = (ASTPtRootNode) _parseTrees
                        .get(_destinationNames.indexOf(_name));

                Set set = _variableCollector.collectFreeVariables(parseTree,
                        _getParserScope());
                List termList = new LinkedList();

                for (Iterator elements = set.iterator(); elements.hasNext();) {
                    String name = (String) elements.next();
                    InequalityTerm term = _getParserScope().getTypeTerm(name);

                    if (term != null && term.isSettable()) {
                        termList.add(term);
                    }
                }

                return (InequalityTerm[]) termList
                        .toArray(new InequalityTerm[termList.size()]);
            } catch (IllegalActionException ex) {
                return new InequalityTerm[0];
            }
        }

        /** Override the base class to give a description of this term.
         *  @return A description of this term.
         */
        @Override
        public String getVerboseString() {
            return getExpression(_name);
        }

        ///////////////////////////////////////////////////////////////
        ////                      private inner variables          ////

        private String _name;

        private ParseTreeTypeInference _typeInference = new ParseTreeTypeInference();

        private ParseTreeFreeVariableCollector _variableCollector = new ParseTreeFreeVariableCollector();
    }

    /** List of channels. Elements may be numbers or variable names. */
    private List _numbers;
}
