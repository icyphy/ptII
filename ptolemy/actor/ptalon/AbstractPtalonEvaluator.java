/* A helper class to store information, like variable scope info, for
 the Ptalon compiler.

 Copyright (c) 2007-2014 The Regents of the University of California.
 All rights reserved.

 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the
 above copyright notice and the following two paragraphs appear in all
 copies of this software.

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
package ptolemy.actor.ptalon;

import java.net.URL;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ptolemy.actor.TypedIOPort;
import ptolemy.actor.TypedIORelation;
import ptolemy.data.BooleanToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.ParseTreeEvaluator;
import ptolemy.data.expr.ParserScope;
import ptolemy.data.expr.PtParser;
import ptolemy.data.type.Type;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.util.StringUtilities;
import antlr.RecognitionException;

/**
 * A helper class to store information, like variable scope info, for
 * the Ptalon compiler.  This class manages references to all elements
 * created as a result of parsing a Ptalon file, and whether they have
 * been instantiated in Ptolemy.
 *
 * @author Adam Cataldo, Elaine Cheong
 * @version $Id$
 * @since Ptolemy II 6.1
 * @Pt.ProposedRating Yellow (celaine)
 * @Pt.AcceptedRating Yellow (celaine)
 */

public abstract class AbstractPtalonEvaluator {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create a new AbstractPtalonEvaluator in the specified
     *  PtalonActor.
     *  @param actor The actor to manage the code for.
     */
    public AbstractPtalonEvaluator(PtalonActor actor) {
        _actor = actor;
        _counter = 0;
        _root = new IfTree(null, _getNextIfSymbol());
        _imports = new Hashtable<String, URL>();
        _currentIfTree = _root;
    }

    /** Add a PtalonParameter to the PtalonActor with the specified
     *  name.
     *
     *  @param name The name of the parameter.
     *  @exception PtalonRuntimeException If the symbol does not
     *  exist, or if the symbol already has a parameter associated
     *  with it, or if an IllegalActionException is thrown trying to
     *  create the parameter.
     */
    public void addActorParameter(String name) throws PtalonRuntimeException {
        try {
            String uniqueName = null;

            // If the parameter already exists and was assigned a
            // value, then use it.
            Attribute attribute = _actor.getAttribute(name);
            if (attribute != null) {
                if (attribute instanceof PtalonParameter) {
                    PtalonParameter parameter = (PtalonParameter) attribute;
                    if (parameter.hasValue()) {
                        uniqueName = name;
                    }
                }
            }

            // If attribute was not found above, create a new one.
            if (uniqueName == null) {
                uniqueName = _actor.uniqueName(name);
                new PtalonParameter(_actor, uniqueName);
            }

            _currentIfTree.setStatus(name, true);
            if (_inNewWhileIteration()) {
                if (_currentIfTree.isForStatement) {
                    _currentIfTree.setEnteredIteration(name,
                            _currentIfTree.entered);
                } else {
                    IfTree tree = _currentIfTree;
                    while (!tree.isForStatement) {
                        tree = tree.getParent();
                        if (tree == null) {
                            throw new PtalonRuntimeException(
                                    "In a new for iteration, "
                                            + "but there is no containing for block.");
                        }
                    }
                    _currentIfTree.setEnteredIteration(name, tree.entered);
                }
            } else {
                _currentIfTree
                        .setEnteredIteration(name, _currentIfTree.entered);
            }
            _currentIfTree.mapName(name, uniqueName);
        } catch (NameDuplicationException ex) {
            throw new PtalonRuntimeException("NameDuplicationException", ex);
        } catch (IllegalActionException ex) {
            throw new PtalonRuntimeException("IllegalActionException", ex);
        }
    }

    /** Add an invisible PtalonParameter to the PtalonActor with the
     *  specified name.
     *
     *  @param name The name of the parameter.
     *  @param expression The expression representing the parameter.
     *  @exception PtalonRuntimeException If the symbol does not
     *  exist, or if the symbol already has a parameter associated
     *  with it, or if an IllegalActionException is thrown trying to
     *  create the parameter.
     */
    public void addActorParameter(String name, String expression)
            throws PtalonRuntimeException {
        try {
            String uniqueName = null;
            PtalonParameter parameter = null;

            // If the parameter already exists and was assigned a
            // value, then use it.
            Attribute attribute = _actor.getAttribute(name);
            if (attribute != null) {
                if (attribute instanceof PtalonParameter) {
                    parameter = (PtalonParameter) attribute;
                    if (parameter.hasValue()) {
                        uniqueName = name;
                    }
                }
            }

            // If attribute was not found above, create a new one.
            if (uniqueName == null) {
                uniqueName = _actor.uniqueName(name);
                parameter = new PtalonParameter(_actor, uniqueName);
            }

            parameter.setVisibility(Settable.NONE);
            _currentIfTree.setStatus(name, true);
            if (_inNewWhileIteration()) {
                if (_currentIfTree.isForStatement) {
                    _currentIfTree.setEnteredIteration(name,
                            _currentIfTree.entered);
                } else {
                    IfTree tree = _currentIfTree;
                    while (!tree.isForStatement) {
                        tree = tree.getParent();
                        if (tree == null) {
                            throw new PtalonRuntimeException(
                                    "In a new for iteration, "
                                            + "but there is no containing for block.");
                        }
                    }
                    _currentIfTree.setEnteredIteration(name, tree.entered);
                }
            } else {
                _currentIfTree
                        .setEnteredIteration(name, _currentIfTree.entered);
            }
            _currentIfTree.mapName(name, uniqueName);
            _unassignedParameters.add(parameter);
            _unassignedParameterValues.add(expression);
        } catch (NameDuplicationException ex) {
            throw new PtalonRuntimeException("NameDuplicationException", ex);
        } catch (IllegalActionException ex) {
            throw new PtalonRuntimeException("IllegalActionException", ex);
        }
    }

    /** Add a TypedIOPort to the PtalonActor with the specified name,
     *  and input flow type.
     *
     *  @param name The name of the port.
     *  @exception PtalonRuntimeException If the symbol does not
     *  exist, or if the symbol already has a port associated with it,
     *  or if an IllegalActionException is thrown trying to create the
     *  port.
     */
    public void addInPort(String name) throws PtalonRuntimeException {
        try {
            String uniqueName = null;
            TypedIOPort port = null;

            // If the port already exists, then use it.
            Port oldPort = _actor.getPort(name);
            if (oldPort != null && oldPort instanceof TypedIOPort) {
                uniqueName = name;
                port = (TypedIOPort) oldPort;
            } else {
                // If port was not found above, create a new one.
                uniqueName = _actor.uniqueName(name);
                port = new TypedIOPort(_actor, uniqueName);
            }
            port.setInput(true);
            port.setOutput(false);
            if (_currentIfTree.getType(name).equals("multiinport")) {
                port.setMultiport(true);
            }
            _currentIfTree.setStatus(name, true);
            if (_inNewWhileIteration()) {
                if (_currentIfTree.isForStatement) {
                    _currentIfTree.setEnteredIteration(name,
                            _currentIfTree.entered);
                } else {
                    IfTree tree = _currentIfTree;
                    while (!tree.isForStatement) {
                        tree = tree.getParent();
                        if (tree == null) {
                            throw new PtalonRuntimeException(
                                    "In a new for iteration, "
                                            + "but there is no containing for block.");
                        }
                    }
                    _currentIfTree.setEnteredIteration(name, tree.entered);
                }
            } else {
                _currentIfTree
                        .setEnteredIteration(name, _currentIfTree.entered);
            }
            _currentIfTree.mapName(name, uniqueName);
        } catch (NameDuplicationException ex) {
            throw new PtalonRuntimeException("NameDuplicationException", ex);
        } catch (IllegalActionException ex) {
            throw new PtalonRuntimeException("IllegalActionException", ex);
        } catch (PtalonScopeException ex) {
            throw new PtalonRuntimeException("Couldn't find symbol " + name, ex);
        }
    }

    /** Add a TypedIOPort to the PtalonActor with the specified name,
     *  and output flow type.
     *
     *  @param name The name of the port.
     *  @exception PtalonRuntimeException If the symbol does not
     *  exist, or if the symbol already has a port associated with it,
     *  or if an IllegalActionException is thrown trying to create the
     *  port.
     */
    public void addOutPort(String name) throws PtalonRuntimeException {
        try {
            String uniqueName = null;
            TypedIOPort port = null;

            // If the port already exists, then use it.
            Port oldPort = _actor.getPort(name);
            if (oldPort != null && oldPort instanceof TypedIOPort) {
                uniqueName = name;
                port = (TypedIOPort) oldPort;
            } else {
                // If port was not found above, create a new one.
                uniqueName = _actor.uniqueName(name);
                port = new TypedIOPort(_actor, uniqueName);
            }
            port.setInput(false);
            port.setOutput(true);
            if (_currentIfTree.getType(name).equals("multioutport")) {
                port.setMultiport(true);
            }
            _currentIfTree.setStatus(name, true);
            if (_inNewWhileIteration()) {
                if (_currentIfTree.isForStatement) {
                    _currentIfTree.setEnteredIteration(name,
                            _currentIfTree.entered);
                } else {
                    IfTree tree = _currentIfTree;
                    while (!tree.isForStatement) {
                        tree = tree.getParent();
                        if (tree == null) {
                            throw new PtalonRuntimeException(
                                    "In a new for iteration, "
                                            + "but there is no containing for block.");
                        }
                    }
                    _currentIfTree.setEnteredIteration(name, tree.entered);
                }
            } else {
                _currentIfTree
                        .setEnteredIteration(name, _currentIfTree.entered);
            }
            _currentIfTree.mapName(name, uniqueName);
        } catch (NameDuplicationException ex) {
            throw new PtalonRuntimeException("NameDuplicationException", ex);
        } catch (IllegalActionException ex) {
            throw new PtalonRuntimeException("IllegalActionException", ex);
        } catch (PtalonScopeException ex) {
            throw new PtalonRuntimeException("Couldn't find symbol " + name, ex);
        }
    }

    /** Add a Parameter to the PtalonActor with the specified name.
     *
     *  @param name The name of the parameter.
     *  @exception PtalonRuntimeException If the symbol does not
     *  exist, or if the symbol already has a parameter associated
     *  with it, or if an IllegalActionException is thrown trying to
     *  create the parameter.
     */
    public void addParameter(String name) throws PtalonRuntimeException {
        try {
            String uniqueName = null;

            // If the parameter already exists and was assigned a
            // value, then use it.
            Attribute attribute = _actor.getAttribute(name);
            if (attribute != null) {
                if (attribute instanceof PtalonExpressionParameter) {
                    PtalonExpressionParameter parameter = (PtalonExpressionParameter) attribute;
                    if (parameter.hasValue()) {
                        uniqueName = name;
                    }
                }
            }

            // If attribute was not found above, create a new one.
            if (uniqueName == null) {
                uniqueName = _actor.uniqueName(name);
                new PtalonExpressionParameter(_actor, uniqueName);
            }

            _currentIfTree.setStatus(name, true);
            if (_inNewWhileIteration()) {
                if (_currentIfTree.isForStatement) {
                    _currentIfTree.setEnteredIteration(name,
                            _currentIfTree.entered);
                } else {
                    IfTree tree = _currentIfTree;
                    while (!tree.isForStatement) {
                        tree = tree.getParent();
                        if (tree == null) {
                            throw new PtalonRuntimeException(
                                    "In a new for iteration, "
                                            + "but there is no containing for block.");
                        }
                    }
                    _currentIfTree.setEnteredIteration(name, tree.entered);
                }
            } else {
                _currentIfTree
                        .setEnteredIteration(name, _currentIfTree.entered);
            }

            _currentIfTree.mapName(name, uniqueName);
        } catch (NameDuplicationException ex) {
            throw new PtalonRuntimeException("NameDuplicationException", ex);
        } catch (IllegalActionException ex) {
            throw new PtalonRuntimeException("IllegalActionException", ex);
        }
    }

    /** Add an invisible Parameter to the PtalonActor with the
     *  specified name and the given expression as its value.
     *
     *  @param name The name of the parameter.
     *  @param expression The expression representing the parameter.
     *  @exception PtalonRuntimeException If the symbol does not
     *  exist, or if the symbol already has a parameter associated
     *  with it, or if an IllegalActionException is thrown trying to
     *  create the parameter.
     */
    public void addParameter(String name, String expression)
            throws PtalonRuntimeException {
        try {
            String uniqueName = null;
            PtalonExpressionParameter parameter = null;

            // If the parameter already exists and was assigned a
            // value, then use it.
            Attribute attribute = _actor.getAttribute(name);
            if (attribute != null) {
                if (attribute instanceof PtalonExpressionParameter) {
                    parameter = (PtalonExpressionParameter) attribute;
                    if (parameter.hasValue()) {
                        uniqueName = name;
                    }
                }
            }

            // If attribute was not found above, create a new one.
            if (uniqueName == null) {
                uniqueName = _actor.uniqueName(name);
                parameter = new PtalonExpressionParameter(_actor, uniqueName);
            }

            parameter.setVisibility(Settable.NONE);
            _currentIfTree.setStatus(name, true);
            if (_inNewWhileIteration()) {
                if (_currentIfTree.isForStatement) {
                    _currentIfTree.setEnteredIteration(name,
                            _currentIfTree.entered);
                } else {
                    IfTree tree = _currentIfTree;
                    while (!tree.isForStatement) {
                        tree = tree.getParent();
                        if (tree == null) {
                            throw new PtalonRuntimeException(
                                    "In a new for iteration, "
                                            + "but there is no containing for block.");
                        }
                    }
                    _currentIfTree.setEnteredIteration(name, tree.entered);
                }
            } else {
                _currentIfTree
                        .setEnteredIteration(name, _currentIfTree.entered);
            }
            _currentIfTree.mapName(name, uniqueName);
            if (_resetParameters || !parameter.hasValue()) {
                _unassignedParameters.add(parameter);
                _unassignedParameterValues.add(expression);
            }
        } catch (NameDuplicationException ex) {
            throw new PtalonRuntimeException("NameDuplicationException", ex);
        } catch (IllegalActionException ex) {
            throw new PtalonRuntimeException("IllegalActionException", ex);
        }
    }

    /** Add a TypedIOPort to the PtalonActor with the specified name.
     *
     *  @param name The name of the port.
     *  @exception PtalonRuntimeException If the symbol does not
     *  exist, or if the symbol already has a port associated with it,
     *  or if an IllegalActionException is thrown trying to create the
     *  port.
     */
    public void addPort(String name) throws PtalonRuntimeException {
        try {
            String uniqueName = null;
            TypedIOPort port = null;

            // If the port already exists, then use it.
            Port oldPort = _actor.getPort(name);
            if (oldPort != null && oldPort instanceof TypedIOPort) {
                uniqueName = name;
                port = (TypedIOPort) oldPort;
            } else {
                // If port was not found above, create a new one.
                uniqueName = _actor.uniqueName(name);
                port = new TypedIOPort(_actor, uniqueName);
            }
            port.setInput(true);
            port.setOutput(true);
            if (_currentIfTree.getType(name).equals("multiport")) {
                port.setMultiport(true);
            }
            _currentIfTree.setStatus(name, true);
            if (_inNewWhileIteration()) {
                if (_currentIfTree.isForStatement) {
                    _currentIfTree.setEnteredIteration(name,
                            _currentIfTree.entered);
                } else {
                    IfTree tree = _currentIfTree;
                    while (!tree.isForStatement) {
                        tree = tree.getParent();
                        if (tree == null) {
                            throw new PtalonRuntimeException(
                                    "In a new for iteration, "
                                            + "but there is no containing for block.");
                        }
                    }
                    _currentIfTree.setEnteredIteration(name, tree.entered);
                }
            } else {
                _currentIfTree
                        .setEnteredIteration(name, _currentIfTree.entered);
            }
            _currentIfTree.mapName(name, uniqueName);
        } catch (NameDuplicationException ex) {
            throw new PtalonRuntimeException("NameDuplicationException", ex);
        } catch (IllegalActionException ex) {
            throw new PtalonRuntimeException("IllegalActionException", ex);
        } catch (PtalonScopeException ex) {
            throw new PtalonRuntimeException("Couldn't find symbol " + name, ex);
        }
    }

    /** Add a TypedIORelation to the PtalonActor with the specified name.
     *
     *  @param name The name of the relation.
     *  @exception PtalonRuntimeException If the symbol does not
     *  exist, or if the symbol already has a relation associated with
     *  it, or if an IllegalActionException is thrown trying to create
     *  the relation.
     */
    public void addRelation(String name) throws PtalonRuntimeException {
        String uniqueName = _actor.uniqueName(name);
        try {
            // In the event that this actor is being reparsed, we
            // don't have to check for a pre-existing relation with
            // this name because we already removed all of the
            // relations in attributeChanged().
            TypedIORelation relation = new TypedIORelation(_actor, uniqueName);
            relation.setWidth(1); // Set explicitly to 1 (the old default) to not break existing models
            _processAttributes(relation);

            _currentIfTree.setStatus(name, true);
            if (_inNewWhileIteration()) {
                if (_currentIfTree.isForStatement) {
                    _currentIfTree.setEnteredIteration(name,
                            _currentIfTree.entered);
                } else {
                    IfTree tree = _currentIfTree;
                    while (!tree.isForStatement) {
                        tree = tree.getParent();
                        if (tree == null) {
                            throw new PtalonRuntimeException(
                                    "In a new for iteration, "
                                            + "but there is no containing for block.");
                        }
                    }
                    _currentIfTree.setEnteredIteration(name, tree.entered);
                }
            } else {
                _currentIfTree
                        .setEnteredIteration(name, _currentIfTree.entered);
            }
            _currentIfTree.mapName(name, uniqueName);
        } catch (NameDuplicationException ex) {
            throw new PtalonRuntimeException("NameDuplicationException", ex);
        } catch (IllegalActionException ex) {
            throw new PtalonRuntimeException("IllegalActionException", ex);
        }
    }

    /** Add a symbol with the given name and type to the symbol table
     *  at the current level of the if-tree hierarchy.
     *
     *  @param name The symbol name.
     *  @param type The symbol type.
     *  @exception PtalonScopeException If a symbol with this name has
     *  already been added somewhere in the current scope.
     */
    public void addSymbol(String name, String type) throws PtalonScopeException {
        List<IfTree> ancestors = _currentIfTree.getAncestors();
        for (IfTree tree : ancestors) {
            for (String symbol : tree.getSymbols()) {
                if (symbol.equals(name)) {
                    throw new PtalonScopeException("Cannot add " + type + " "
                            + name + " because symbol " + name
                            + " already exists in scope " + tree.getName());
                }
            }
        }
        _currentIfTree.addSymbol(name, type);
    }

    /** Add a transparent relation to the PtalonActor with the
     *  specified name. A transparent relation is not the same as a
     *  relation. Rather, it provides a means for connecting multiple
     *  ports to an input port. It is transparent in that if this
     *  transparent relation is connected to an input port I, then any
     *  connections made to the transparent relation will be as if
     *  they were connected directly to I, instead of through a
     *  relation.
     *
     *  @param name The name of the relation.
     *  @exception PtalonRuntimeException If in a new for iteration
     *  but no containing for block is found.
     */
    public void addTransparentRelation(String name)
            throws PtalonRuntimeException {
        _currentIfTree.setStatus(name, true);
        if (_inNewWhileIteration()) {
            if (_currentIfTree.isForStatement) {
                _currentIfTree
                        .setEnteredIteration(name, _currentIfTree.entered);
            } else {
                IfTree tree = _currentIfTree;
                while (!tree.isForStatement) {
                    tree = tree.getParent();
                    if (tree == null) {
                        throw new PtalonRuntimeException(
                                "In a new for iteration, "
                                        + "but there is no containing for block.");
                    }
                }
                _currentIfTree.setEnteredIteration(name, tree.entered);
            }
        } else {
            _currentIfTree.setEnteredIteration(name, _currentIfTree.entered);
        }
    }

    /** Assign any internal parameters in the order they were set.
     *
     *  @exception PtalonRuntimeException If there is any trouble
     *  assigning parameter values.
     */
    public void assignInternalParameters() throws PtalonRuntimeException {
        try {
            while (!_unassignedParameters.isEmpty()) {
                PtalonParameter parameter = _unassignedParameters.remove(0);
                String expression = _unassignedParameterValues.remove(0);
                String oldExpression = parameter.getExpression();
                if (!expression.equals(oldExpression)) {
                    parameter.setToken(expression);
                }
            }
        } catch (Exception ex) {
            throw new PtalonRuntimeException("Trouble assigning parameter", ex);
        }
    }

    /** Enter the named for-block subscope.
     *
     *  @param scope The named subscope.
     *  @param forBlock The AST for the subscope.
     *  @param populator The PtalonPopulator that called this statement.
     *  @exception PtalonRuntimeException If the subscope does not exist.
     */
    public void enterForScope(String scope, PtalonAST forBlock,
            PtalonPopulator populator) throws PtalonRuntimeException {
        enterIfScope(scope);
        _currentIfTree.forBlock = forBlock;
        _currentIfTree.populator = populator;
    }

    /** Enter the named subscope.
     *
     *  @param scope The named subscope.
     *  @exception PtalonRuntimeException If the subscope does not exist.
     */
    public void enterIfScope(String scope) throws PtalonRuntimeException {
        boolean exists = false;
        for (IfTree tree : _currentIfTree.getChildren()) {
            if (tree.getName().equals(scope)) {
                exists = true;
                _currentIfTree = tree;
                break;
            }
        }
        if (!exists) {
            throw new PtalonRuntimeException("Subscope " + scope
                    + " does not exist");
        }
    }

    public void enterTransformation(boolean incremental)
            throws PtalonRuntimeException {
        throw new PtalonRuntimeException("Transformation is not implemented.");
    }

    /** Evaluate the given expression and return its boolean
     *  value. The expression should return a boolean value, otherwise
     *  an exception is thrown.
     *
     *  @param expression The expression to evaluate.
     *  @return The boolean result of evaluation.
     *  @exception PtalonRuntimeException If the result is not a
     *  boolean.
     */
    public boolean evaluateBoolean(String expression)
            throws PtalonRuntimeException {
        try {
            BooleanToken result = (BooleanToken) evaluateExpression(expression);
            return result.booleanValue();
        } catch (ClassCastException ex) {
            throw new PtalonRuntimeException("Not a boolean token.");
        }
    }

    /** Evaluate the given expression and return the corresponding token.
     *
     *  @param expression The expression to evaluate.
     *  @return The result of evaluation.
     *  @exception PtalonRuntimeException If unable to evaluate the
     *  expression.
     */
    public Token evaluateExpression(String expression)
            throws PtalonRuntimeException {
        try {
            PtParser parser = new PtParser();

            ParseTreeEvaluator _parseTreeEvaluator = new ParseTreeEvaluator();
            ASTPtRootNode _parseTree = parser.generateParseTree(expression);
            Token result = _parseTreeEvaluator.evaluateParseTree(_parseTree,
                    _scope);
            return result;
        } catch (Exception ex) {
            throw new PtalonRuntimeException("Unable to evaluate expression\n"
                    + expression, ex);
        }
    }

    /** Evaluate the current for block, assuming there is one.
     *
     *  @exception PtalonRuntimeException If there is any trouble
     *  evaluating this for block.
     */
    public void evaluateForScope() throws PtalonRuntimeException {
        if (!_currentIfTree.isForStatement) {
            throw new PtalonRuntimeException("Not in a for statement");
        }
        _currentIfTree.evaluateForScope();
    }

    /** Evaluate the given input expression and return a string
     *  representation of it, or null, if there is some reason it
     *  cannot be evaluated.
     *
     *  @param expression The input expression.
     *  @return The evaluated value, or null if evaluation is not
     *  possible.
     */
    public String evaluateString(String expression) {
        try {
            PtParser parser = new PtParser();
            ParseTreeEvaluator _parseTreeEvaluator = new ParseTreeEvaluator();
            ASTPtRootNode _parseTree = parser.generateParseTree(expression);
            Token result = _parseTreeEvaluator.evaluateParseTree(_parseTree,
                    _scope);
            if (result instanceof StringToken) {
                return ((StringToken) result).stringValue();
            }
            return result.toString();
        } catch (IllegalActionException ex) {
            return null;
        }
    }

    /** Exit the current for scope.
     *
     *  @exception PtalonRuntimeException If not in a for-block scope.
     */
    public void exitForScope() throws PtalonRuntimeException {
        if (!_currentIfTree.isForStatement) {
            throw new PtalonRuntimeException("Not in a for-block.");
        }
        exitIfScope();
    }

    /** Exit the current if scope.
     *
     *  @exception PtalonRuntimeException If already at the top-level
     *  if scope.
     */
    public void exitIfScope() throws PtalonRuntimeException {
        if (_currentIfTree.getParent() == null) {
            throw new PtalonRuntimeException("Already at top level");
        }
        _currentIfTree = _currentIfTree.getParent();
    }

    public void exitTransformation() throws PtalonRuntimeException {
        throw new PtalonRuntimeException("Transformation is not implemented.");
    }

    /** Get the unique name for the symbol in the PtalonActor.
     *
     *  @param symbol The symbol to test.
     *  @return The unique name.
     *  @exception PtalonRuntimeException If no such symbol exists.
     */
    public String getMappedName(String symbol) throws PtalonRuntimeException {
        for (IfTree tree : _currentIfTree.getAncestors()) {
            try {
                String output = tree.getDeepMappedName(symbol);
                return output;
            } catch (PtalonRuntimeException ex) {
            }
        }
        throw new PtalonRuntimeException("Could not find mapped name for"
                + symbol);
    }

    /** Return whether there are unassigned parameters to be handled with the
     *  {@link #assignInternalParameters()} method.
     *
     *  @return true if there are unassigned parameters, and false otherwise.
     */
    public boolean hasUnassignedParameters() {
        return !_unassignedParameters.isEmpty();
    }

    /** Return true if the given symbol exists in the current scope.
     *
     *  @param symbol The symbol to test.
     *  @return true If the given symbol exists in the current scope.
     */
    public boolean inScope(String symbol) {
        List<IfTree> ancestors = _currentIfTree.getAncestors();
        for (IfTree tree : ancestors) {
            if (tree.getSymbols().contains(symbol)) {
                return true;
            }
        }
        return _currentIfTree.inDeepScope(symbol);
    }

    /** Return true if an entity was created in PtalonActor for the
     *  given symbol.  This symbol is assumed to be in the current
     *  scope.
     *
     *  @param symbol The symbol to test.
     *  @return true If an entity was created for this symbol.
     *  @exception PtalonRuntimeException If the symbol is not in the
     *  current scope.
     */
    public boolean isCreated(String symbol) throws PtalonRuntimeException {
        if (_inNewWhileIteration()) {
            if (_currentIfTree.isForStatement) {
                int iteration = _currentIfTree.getEnteredIteration(symbol);
                if (iteration == 0) {
                    return false;
                }
                if (iteration == _currentIfTree.entered) {
                    return false;
                }
                return true;
            } else {
                IfTree tree = _currentIfTree;
                while (!tree.isForStatement) {
                    tree = tree.getParent();
                    if (tree == null) {
                        throw new PtalonRuntimeException(
                                "In a new for iteration, "
                                        + "but there is no containing for block.");
                    }
                }
                int iteration = tree.getEnteredIteration(symbol);
                if (iteration == 0) {
                    return false;
                }
                if (iteration == tree.entered) {
                    return false;
                }
                return true;
            }
        }
        List<IfTree> ancestors = _currentIfTree.getAncestors();
        for (IfTree parent : ancestors) {
            if (parent.isCreated(symbol)) {
                return true;
            }
        }
        return false;
    }

    /** Return true if the boolean for the current conditional is
     *  ready to be entered. It is ready when all ports, parameters,
     *  and relations in the containing scope have been created, when
     *  all parameters in the containing scope have been assigned
     *  values, and when in a branch of an if-block or for-block that
     *  is active.
     *
     *  @return true If the current for-block scope is ready to be entered.
     *  @exception PtalonRuntimeException If it is thrown trying to
     *  access a parameter.
     */
    public boolean isForReady() throws PtalonRuntimeException {
        return isIfReady();
    }

    /** Return true if the boolean for the current conditional is
     *  ready to be entered. It is ready when all ports, parameters,
     *  and relations in the containing scope have been created, when
     *  all parameters in the containing scope have been assigned
     *  values, and when in a branch of an if-block that is active.
     *
     *  @return true If the current if-block scope is ready to be entered.
     *  @exception PtalonRuntimeException If it is thrown trying to
     *  access a parameter.
     */
    public boolean isIfReady() throws PtalonRuntimeException {
        IfTree parent = _currentIfTree.getParent();
        if (parent == null) {
            return false; // Should never make it here.
        } else if (parent.getActiveBranch() == null) {
            return false;
        } else if (parent.getActiveBranch() != parent.getCurrentBranch()) {
            return false;
        }
        List<IfTree> ancestors = parent.getAncestors();
        for (IfTree tree : ancestors) {
            if (!tree.isFullyAssigned()) {
                return false;
            }
        }
        return true;
    }

    /** Return true if the current piece of code is ready to be
     *  entered. This is used by port, parameter, and relation
     *  declarations only. It is ready when all ports, parameters, and
     *  relations in the containing scope have been created, when all
     *  parameters in the containing scope have been assigned values,
     *  and when in a branch of an if-block that is active.
     *
     *  @return true If the current if-block scope is ready to be entered.
     *  @exception PtalonRuntimeException If it is thrown trying to
     *  access a parameter.
     */
    public boolean isReady() throws PtalonRuntimeException {
        IfTree parent = _currentIfTree.getParent();
        if (parent == null) {
            return true;
        }
        List<IfTree> ancestors = parent.getAncestors();
        for (IfTree tree : ancestors) {
            if (!tree.isFullyAssigned()) {
                return false;
            }
        }
        if (_currentIfTree.getActiveBranch() == null) {
            return false;
        }
        return _currentIfTree.getActiveBranch() == _currentIfTree
                .getCurrentBranch();
    }

    public void negateObject(String name) throws PtalonRuntimeException {
        throw new PtalonRuntimeException("Negation is not implemented.");
    }

    public void optionalObject(String name) throws PtalonRuntimeException {
        throw new PtalonRuntimeException("Optional objects are not "
                + "implemented.");
    }

    /** Pop out of the scope of the current for statement and into its
     *  container block's scope.
     *
     *  @return The unique name of the for-statement block being exited.
     *  @exception PtalonScopeException If the current scope is
     *  already the outermost scope.
     */
    public String popForStatement() throws PtalonScopeException {
        return popIfStatement();
    }

    /** Pop out of the scope of the current if statement and into its
     *  container block's scope.
     *
     *  @return The unique name of the if-statement block being exited.
     *  @exception PtalonScopeException If the current scope is
     *  already the outermost scope.
     */
    public String popIfStatement() throws PtalonScopeException {
        String name = _currentIfTree.getName();
        _currentIfTree = _currentIfTree.getParent();
        if (_currentIfTree == null) {
            throw new PtalonScopeException(
                    "Attempt to pop out of outermost scope");
        }
        return name;
    }

    public void preserveObject(String name) throws PtalonRuntimeException {
        throw new PtalonRuntimeException("Preservation is not implemented.");
    }

    /** Push into the scope of a new for statement contained as a
     *  sub-block of the current (FIXME: if or for) statement.
     *
     *  @param variable The variable associated with the for
     *  statement.
     *  @param initExpr The expression representing the initial value
     *  for the variable.
     *  @param satExpr The expression evaluated before executing the
     *  for statement body.
     */
    public void pushForStatement(String variable, String initExpr,
            String satExpr) {
        String name = _getNextIfSymbol();
        _currentIfTree = _currentIfTree.addChild(name);
        _currentIfTree.isForStatement = true;
        _currentIfTree.variable = variable;
        _currentIfTree.initExpr = initExpr;
        _currentIfTree.satExpr = satExpr;
    }

    /** Push into the scope of a new if statement contained as a
     *  sub-block of the current if statement.
     */
    public void pushIfStatement() {
        String name = _getNextIfSymbol();
        _currentIfTree = _currentIfTree.addChild(name);
    }

    public void removeObject(String name) throws PtalonRuntimeException {
        throw new PtalonRuntimeException("Removing is not implemented.");
    }

    /** Set the active branch for the current if statement.
     *
     *  @param branch The branch to set.
     */
    public void setActiveBranch(boolean branch) {
        _currentIfTree.setActiveBranch(branch);
        _currentIfTree.entered += 1;
    }

    /** Set the symbol in the PtalonCode which represents this
     *  AbstractPtalonEvaluator's actor.
     *
     *  @param symbol The name of this actor in the Ptalon file.
     *  @exception PtalonScopeException If the symbol has been added
     *  already, or if there is some problem accessing its associated
     *  file.
     */
    public void setActorSymbol(String symbol) throws PtalonScopeException {
        _root.addSymbol(symbol, "import");
        _root.setStatus(symbol, true);
        try {
            // Use asURL() so that $CLASSPATH is expanded
            _imports.put(symbol, _actor.ptalonCodeLocation.asURL());
            if (!_actor.getName().startsWith(symbol)) {
                String uniqueName = _actor.getContainer().uniqueName(symbol);
                _actor.setName(uniqueName);
            }
        } catch (Exception ex) {
            throw new PtalonScopeException("Unable to access file for "
                    + symbol, ex);
        }
    }

    /** Set the current branch that's being walked.
     *
     *  @param branch True if the true branch is being walked.
     */
    public void setCurrentBranch(boolean branch) {
        _currentIfTree.setCurrentBranch(branch);
    }

    /** Set the next expression for the current for statement scope,
     *  assuming the current scope is a for statement and not an if
     *  statement.
     *
     *  @param nextExpr The expression to represent the next statement.
     */
    public void setNextExpression(String nextExpr) {
        _currentIfTree.nextExpr = nextExpr;
    }

    /** Prepare the compiler to start at the outermost scope of the
     *  Ptalon program during run time.
     */
    public void startAtTop() {
        _currentIfTree = _root;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return a number of spaces that is proportional to the
     *  argument. If the argument is negative or zero, return an empty
     *  string.
     *
     *  @param level The level of indenting represented by the spaces.
     *  @return A string with zero or more spaces.
     */
    protected static String _getIndentPrefix(int level) {
        return StringUtilities.getIndentPrefix(level);
    }

    /** Return the number of times the current if tree has been
     *  entered.
     *
     *  @return The number of times the current if tree has been entered.
     */
    protected int _getTimesEntered() {
        return _currentIfTree.entered;
    }

    /** Return the type associated with the given symbol in the
     *  current scope.
     *
     *  @param symbol The symbol under test.
     *  @return The type associated with the given symbol.
     *  @exception PtalonScopeException If the symbol is not in the
     *  current scope.
     */
    protected String _getType(String symbol) throws PtalonScopeException {
        List<IfTree> ancestors = _currentIfTree.getAncestors();
        for (IfTree tree : ancestors) {
            try {
                String type = tree.getDeepType(symbol);
                return type;
            } catch (PtalonScopeException ex) {
                // Do nothing here, just go on to check the next if-block
                // sub-scope
            }
        }
        throw new PtalonScopeException("Symbol " + symbol
                + " not in current scope.");
    }

    /** Return the type associated with the given symbol in the
     *  current scope.  This is the same as getType, but it is used to
     *  avoid a name conflict in PtalonEvaluator.PtalonExpressionScope
     *
     *  @param symbol The symbol under test.
     *  @return The type associated with the given symbol.
     *  @exception PtalonScopeException If the symbol is not in the
     *  current scope.
     */
    protected String _getTypeForScope(String symbol)
            throws PtalonScopeException {
        return _getType(symbol);
    }

    /** Return true if in a new iteration of a while block.
     *
     *  @return true If in a new iteration of a while block.
     */
    protected boolean _inNewWhileIteration() {
        return _currentIfTree.inNewWhileIteration();
    }

    protected boolean _isPreservingTransformation() {
        return _isPreservingTransformation;
    }

    /**
     *
     *  @param object
     *  @exception PtalonRuntimeException
     */
    protected void _processAttributes(NamedObj object)
            throws PtalonRuntimeException {
    }

    /** Set whether to reset parameters when the actor is populated and the
     *  parameters already exist for the actor.
     *
     *  @param reset Whether to reset parameters.
     */
    protected void _resetParameters(boolean reset) {
        _resetParameters = reset;
    }

    protected void _setPreservingTransformation(boolean b) {
        _isPreservingTransformation = b;
    }

    ///////////////////////////////////////////////////////////////////
    ////                      protected members                    ////

    /** The actor in which this PtalonCompilerInfo is used.
     */
    protected PtalonActor _actor;

    /** Some descendent of the root tree to which new input symbols
     *  should be added.
     */
    protected IfTree _currentIfTree;

    /** A list of the import symbols and their corresponding files.
     */
    protected Hashtable<String, URL> _imports;

    /** The expression scope for this code manager.
     */
    protected PtalonExpressionScope _scope = new PtalonExpressionScope();

    /** Maps names of transparent relations to ports, which should be
     *  multiports. A key may map to null if no port has been assigned
     *  to it.
     */
    protected Map<String, TypedIOPort> _transparentRelations = new Hashtable<String, TypedIOPort>();

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** This is a representation of an if/else construct in Ptalon.
     *  The true branch and/or the false branch can point to a set of
     *  IfTrees.  This class is used when the keyword "if" appears in
     *  the Ptalon source code.  There are no dangling "if"s in Ptalon,
     *  so this class always knows when to use the true or false branch
     *  in the representation.
     *
     */
    protected class IfTree extends NamedTree<IfTree> {

        /** Create a new if tree.
         *
         *  @param name The name to give this if tree.
         *  @param parent The parent to this tree, which may be null
         *  if this is the root of a tree.
         */
        public IfTree(IfTree parent, String name) {
            super(parent, name);
            _trueNameMappings = new Hashtable<String, String>();
            _trueSetStatus = new Hashtable<String, Boolean>();
            _trueSymbols = new Hashtable<String, String>();
            _falseNameMappings = new Hashtable<String, String>();
            _falseSetStatus = new Hashtable<String, Boolean>();
            _falseSymbols = new Hashtable<String, String>();
        }

        /** Create a new child tree to this tree with the specified
         *  name and return it. Subclasses should override this if
         *  it's not the case.
         *
         *  @param name The name of the child.
         *  @return The child IfTree.
         */
        @Override
        public IfTree addChild(String name) {
            IfTree child = new IfTree(this, name);
            _children.add(child);
            return child;
        }

        /** Add a symbol to the scope of this if statement.
         *
         *  @param symbol The symbol to add.
         *  @param type Its corresponding type.
         */
        public void addSymbol(String symbol, String type) {
            if (_currentBranch || isForStatement) {
                _trueSymbols.put(symbol, type);
                _trueNameMappings.put(symbol, symbol);
                _trueSetStatus.put(symbol, false);
            } else {
                _falseSymbols.put(symbol, type);
                _falseNameMappings.put(symbol, symbol);
                _falseSetStatus.put(symbol, false);
            }
        }

        /** Add a symbol to the scope of this if statement.
         *
         * @param symbol The symbol to add.
         * @param type Its corresponding type.
         * @param status Whether the if statement has been loaded or not.
         * @param uniqueName The unique name of this if statement.
         */
        public void addSymbol(String symbol, String type, boolean status,
                String uniqueName) {
            if (_currentBranch || isForStatement) {
                _trueSymbols.put(symbol, type);
                _trueNameMappings.put(symbol, uniqueName);
                _trueSetStatus.put(symbol, status);
            } else {
                _falseSymbols.put(symbol, type);
                _falseNameMappings.put(symbol, uniqueName);
                _falseSetStatus.put(symbol, status);
            }
        }

        /** Evaluate this for block, assuming this is a for block.
         *
         *  @exception PtalonRuntimeException If there is any trouble
         *  evaluating this for block.
         */
        public void evaluateForScope() throws PtalonRuntimeException {
            if (getParent().isForStatement) {
                entered = 1;
            }
            Token initialValue = evaluateExpression(initExpr);
            _scope.addVariable(variable, initialValue);
            _currentBranch = true;
            _inNewWhileIteration = true;
            while (evaluateBoolean(satExpr)) {
                try {
                    populator.iterative_statement_evaluator(forBlock);
                } catch (RecognitionException ex) {
                    throw new PtalonRuntimeException(
                            "Could not recognize for block", ex);
                }
                Token nextValue = evaluateExpression(nextExpr);
                _scope.addVariable(variable, nextValue);
            }
            _inNewWhileIteration = false;
            _scope.removeVariable(variable);
            _currentBranch = false;
        }

        /** Return the active branch, which may be null if it has not
         *  yet been set.
         *
         *  @return The active branch
         *  @see #setActiveBranch
         */
        public Boolean getActiveBranch() {
            return _activeBranch;
        }

        /** Return the ancestors of this tree, including this tree.
         *
         *  @return The ancestors of this tree, including this tree.
         */
        public List<IfTree> getAncestors() {
            List<IfTree> list = getProperAncestors();
            list.add(this);
            return list;
        }

        /** Return true if we are in the main scope or the true part
         *  of a true branch.
         *
         *  @return true If in main scope or true part of a true branch.
         *  @see #setCurrentBranch
         */
        public boolean getCurrentBranch() {
            return _currentBranch;
        }

        /** Get the unique name for the symbol in the PtalonActor,
         *  looking deep into for loops for potential matches.
         *
         *  @param symbol The symbol to test.
         *  @return The unique name.
         *  @exception PtalonRuntimeException If no such symbol exists.
         */
        public String getDeepMappedName(String symbol)
                throws PtalonRuntimeException {
            try {
                String name = getMappedName(symbol);
                return name;
            } catch (PtalonRuntimeException ex) {
            }
            for (IfTree child : _children) {
                if (child.isForStatement) {
                    try {
                        String name = child.getDeepMappedName(symbol);
                        return name;
                    } catch (PtalonRuntimeException ex) {
                    }
                }
            }
            String message = symbol.concat(" not found.");
            throw new PtalonRuntimeException(message);
        }

        /** Return the type associated with the given symbol, looking
         *  deep into for loops that might add symbols to this scope.
         *
         *  @param symbol The symbol under test.
         *  @return The type associated with the given symbol.
         *  @exception PtalonScopeException If the symbol is not in
         *  the scope of the if statement associated with this IfTree.
         */
        public String getDeepType(String symbol) throws PtalonScopeException {
            try {
                String type = getType(symbol);
                return type;
            } catch (PtalonScopeException ex) {
            }
            for (IfTree child : _children) {
                if (child.isForStatement) {
                    try {
                        String type = child.getDeepType(symbol);
                        return type;
                    } catch (PtalonScopeException ex) {
                    }
                }
            }
            String message = symbol.concat(" not found.");
            throw new PtalonScopeException(message);
        }

        /** Get the iteration (number of times this if/for block has
         *  been entered) in which this symbol is created.
         *
         *  @param symbol The symbol created.
         *  @return The iteration number.
         *  @see #setEnteredIteration
         */
        public int getEnteredIteration(String symbol) {
            Integer entered = _createdIteration.get(symbol);
            if (entered == null) {
                return 0;
            }
            return entered;
        }

        /** Get the unique name for the symbol in the PtalonActor.
         *
         *  @param symbol The symbol to test.
         *  @return The unique name.
         *  @exception PtalonRuntimeException If no such symbol exists.
         */
        public String getMappedName(String symbol)
                throws PtalonRuntimeException {
            String output;
            if (_currentBranch || isForStatement) {
                output = _trueNameMappings.get(symbol);
            } else {
                output = _falseNameMappings.get(symbol);
            }
            if (output == null) {
                throw new PtalonRuntimeException("Symbol " + symbol
                        + " not found.");
            }
            return output;
        }

        /** Return a set of strings representing all symbols in the
         *  scope of the if-block.
         *
         *  @return All symbols in the scope of the if-block.
         */
        public Set<String> getSymbols() {
            if (_currentBranch || isForStatement) {
                return _trueSymbols.keySet();
            } else {
                return _falseSymbols.keySet();
            }
        }

        /** Return the type associated with the given symbol.
         *
         *  @param symbol The symbol under test.
         *  @return The type associated with the given symbol.
         *  @exception PtalonScopeException If the symbol is not in
         *  the scope of the if statement associated with this IfTree.
         */
        public String getType(String symbol) throws PtalonScopeException {
            String value;
            if (_currentBranch || isForStatement) {
                value = _trueSymbols.get(symbol);
            } else {
                value = _falseSymbols.get(symbol);
            }
            if (value == null) {
                String message = symbol.concat(" not found.");
                throw new PtalonScopeException(message);
            }
            return value;
        }

        /** Return true if the given symbol is in this scope, or
         *  deeply in this scope through some for loop.
         *
         *  @param symbol The symbol to test.
         *  @return true If symbol is in the right scope.
         */
        public boolean inDeepScope(String symbol) {
            if ((_currentBranch || isForStatement)
                    && _trueSymbols.containsKey(symbol)) {
                return true;
            } else if (!(_currentBranch || isForStatement)
                    && _falseSymbols.containsKey(symbol)) {
                return true;
            }
            for (IfTree child : _children) {
                if (child.isForStatement) {
                    if (child.inDeepScope(symbol)) {
                        return true;
                    }
                }
            }
            return false;
        }

        /** Return true if in a new iteration of a while block.
         *
         *  @return true If in a new iteration of a while block.
         */
        public boolean inNewWhileIteration() {
            if (isForStatement) {
                return _inNewWhileIteration;
            }
            if (_parent == null) {
                return false;
            }
            return _parent.inNewWhileIteration();
        }

        /** Return true if an entity was created in PtalonActor for
         *  the given symbol. This symbol is assumed to be in the
         *  current scope.
         *
         *  @param symbol The symbol to test.
         *  @return true If an entity was created for this symbol.
         *  @exception PtalonRuntimeException If the symbol is not in
         *  the current scope.
         */
        public boolean isCreated(String symbol) throws PtalonRuntimeException {
            Boolean status;
            if (_currentBranch || isForStatement) {
                status = _trueSetStatus.get(symbol);
            } else {
                status = _falseSetStatus.get(symbol);
            }
            if (status == null) {
                return false;
            }
            return status;
        }

        /** Return true if all the symbols in this if block have been
         *  assigned a value. A symbol has been assigned a value if a
         *  corresponding entity for the symbol has been created in
         *  the PtalonActor, and in the case of parameters, that the
         *  user has provided a value for the parameter.
         *
         *  @return true If all the symbols in this if block have been
         *  assigned a value.
         *  @exception PtalonRuntimeException If there is any problem
         *  accessing a parameter.
         */
        public boolean isFullyAssigned() throws PtalonRuntimeException {
            if (_currentBranch || isForStatement) {
                for (String symbol : _trueSetStatus.keySet()) {
                    if (_trueSymbols.get(symbol).endsWith("parameter")) {
                        if (!_trueSetStatus.get(symbol)) {
                            return false;
                        }
                        try {
                            PtalonParameter param = (PtalonParameter) _actor
                                    .getAttribute(_trueNameMappings.get(symbol));
                            if (!param.hasValue()) {
                                return false;
                            }
                        } catch (Exception ex) {
                            throw new PtalonRuntimeException(
                                    "Could not access parameter " + symbol, ex);
                        }
                    }
                }
                return true;
            } else {
                for (String symbol : _falseSetStatus.keySet()) {
                    if (!_falseSetStatus.get(symbol)) {
                        return false;
                    }
                    if (_falseSymbols.get(symbol).endsWith("parameter")) {
                        try {
                            PtalonParameter param = (PtalonParameter) _actor
                                    .getAttribute(_falseNameMappings
                                            .get(symbol));
                            if (!param.hasValue()) {
                                return false;
                            }
                        } catch (Exception ex) {
                            throw new PtalonRuntimeException(
                                    "Could not access parameter " + symbol, ex);
                        }
                    }
                }
                return true;
            }
        }

        /** Map a name of a symbol from a Ptalon program to a name in
         *  the PtalonActor which creates it.
         *
         *  @param symbol The name for the symbol in the Ptalon program.
         *  @param uniqueName The unique name for the symbol in the
         *  PtalonActor.
         *  @exception PtalonRuntimeException If the symbol does not
         *  exist.
         */
        public void mapName(String symbol, String uniqueName)
                throws PtalonRuntimeException {
            String value;
            if (_currentBranch || isForStatement) {
                value = _trueSymbols.get(symbol);
                if (value == null) {
                    String message = symbol.concat(" not found.");
                    throw new PtalonRuntimeException(message);
                }
                _trueNameMappings.put(symbol, uniqueName);
            } else {
                value = _falseSymbols.get(symbol);
                if (value == null) {
                    String message = symbol.concat(" not found.");
                    throw new PtalonRuntimeException(message);
                }
                _falseNameMappings.put(symbol, uniqueName);
            }
        }

        /** Set the active branch to true or false.
         *
         *  @param branch The branch to set it to.
         *  @see #getActiveBranch
         */
        public void setActiveBranch(boolean branch) {
            _activeBranch = branch;
        }

        /** Set the current branch that's being walked.
         *
         *  @param branch True if the true branch is being walked.
         *  @see #getCurrentBranch
         */
        public void setCurrentBranch(boolean branch) {
            _currentBranch = branch;
        }

        /** Set the iteration (number of times this if/for block has been
         *  entered) in which this symbol is created.
         *
         *  @param symbol The symbol created.
         *  @param iteration The iteration of the symbol.
         *  @see #getEnteredIteration
         */
        public void setEnteredIteration(String symbol, int iteration) {
            _createdIteration.put(symbol, iteration);
        }

        /** Set the status of the symbol to true, if the symbol is
         *  ready, and false otherwise.
         *
         *  @param symbol The symbol.
         *  @param status The status.
         */
        public void setStatus(String symbol, boolean status) {
            if (_currentBranch || isForStatement) {
                _trueSetStatus.put(symbol, status);
            } else {
                _falseSetStatus.put(symbol, status);
            }
        }

        /** Enumerate the info from this scope.
         */
        @Override
        public String toString() {
            StringBuffer buffer = new StringBuffer("Scope: " + getName()
                    + ":\n\n");
            for (String s : getSymbols()) {
                try {
                    buffer.append(s + "\t" + getType(s) + "\n");
                } catch (PtalonScopeException ex) {
                    // FIXME: is this ok to do nothing?
                }
            }
            buffer.append("---------------\n");
            for (IfTree child : getChildren()) {
                buffer.append(child.toString());
            }
            String output = buffer.toString();
            return output;
        }

        /** The number of times the if/for tree has been entered.
         */
        public int entered = 0;

        /** This is the AST for this for block, if this is a for
         *  block.
         */
        public PtalonAST forBlock = null;

        /** This is the initially expression for the for statement, if
         *  this is a for statement.
         */
        public String initExpr = "";

        /** This is true if this if statement is actually used to
         *  represent a for statement.
         */
        public boolean isForStatement = false;

        /** This is the next expression for the for statement, if this
         *  is a for statement.
         */
        public String nextExpr = "";

        /** This is the PtalonPopulator that accesses this for
         *  statement, if this is a for statement.
         */
        public PtalonPopulator populator = null;

        /** This is the satisfies expression for the for statement, if
         *  this is a for statement.
         */
        public String satExpr = "";

        /** This is the variable for the for statement, if this is a
         *  for statement.
         */
        public String variable = "";

        /** This is true when the active branch for this if statement
         *  is true, false when it is false, and null when it is
         *  unknown.
         */
        private Boolean _activeBranch = null;

        /** The number of iterations (number of times the if/for block
         *  has been entered) for each symbol created.
         */
        private Hashtable<String, Integer> _createdIteration = new Hashtable<String, Integer>();

        /** This is true if we are in the main scope or the true part
         *  of a true branch.
         */
        private boolean _currentBranch = true;

        /** Each symbol gets mapped to its unique name in the Ptalon
         *  Actor. This is for the false branch of this if tree.
         */
        private Hashtable<String, String> _falseNameMappings;

        /** A symbol maps to false if it has been set to some value or
         *  false otherwise. This is for the false branch of this if
         *  tree.
         */
        private Hashtable<String, Boolean> _falseSetStatus;

        /** The symbol table for this level of the if hierarchy. This
         *  is for the false branch of this if tree.
         */
        private Hashtable<String, String> _falseSymbols;

        /** This is true if in a new iteration of a while block.
         */
        private boolean _inNewWhileIteration = false;

        /** Each symbol gets mapped to its unique name in the Ptalon
         *  Actor. This is for the true branch of this if tree.
         */
        private Hashtable<String, String> _trueNameMappings;

        /** A symbol maps to true if it has been set to some value or
         *  false otherwise. This is for the true branch of this if
         *  tree.
         */
        private Hashtable<String, Boolean> _trueSetStatus;

        /** The symbol table for this level of the if hierarchy. This
         *  is for the true branch of this if tree.
         */
        private Hashtable<String, String> _trueSymbols;
    }

    /**
     * FIXME comment
     */
    protected class PtalonExpressionScope implements ParserScope {

        /** Add the specified variable with the given value.
         *
         *  @param name The variable name.
         *  @param value The variable's value.
         */
        public void addVariable(String name, Token value) {
            _variables.put(name, value);
        }

        /** Look up and return the value of the variable or parameter
         *  with the specified name in the scope. Return null if the
         *  name is not defined in this scope.
         *
         *  @param name The name of the variable or parameter.
         *  @return The token associated with the given name in the scope.
         *  @exception IllegalActionException If a value in the scope
         *  exists with the given name, but cannot be evaluated.
         */
        @Override
        public Token get(String name) throws IllegalActionException {
            try {
                if (_variables.containsKey(name)) {
                    return _variables.get(name);
                }
                if (!_getTypeForScope(name).equals("parameter")) {
                    throw new IllegalActionException(name + " not a parameter.");
                }
                return _getValueOf(name);
            } catch (PtalonScopeException ex) {
                return null;
            } catch (PtalonRuntimeException ex) {
                return null;
            }
        }

        /** Look up and return the type of the variable or parameter
         *  with the specified name in the scope. Return null if the
         *  name is not defined in this scope.
         *
         *  @param name The name of the variable or parameter.
         *  @return The token associated with the given name in the scope.
         *  @exception IllegalActionException If a value in the scope
         *  exists with the given name, but cannot be evaluated.
         */
        @Override
        public Type getType(String name) throws IllegalActionException {
            try {
                if (_variables.containsKey(name)) {
                    return _variables.get(name).getType();
                }
                if (!_getTypeForScope(name).equals("parameter")) {
                    throw new IllegalActionException(name + " not a parameter.");
                }
                return _getTypeOf(name);
            } catch (PtalonScopeException ex) {
                return null;
            } catch (PtalonRuntimeException ex) {
                return null;
            }
        }

        /** Look up and return the type term for the variable or
         *  parameter with the specified name in the scope. Return
         *  null if the name is not defined in this scope, or is a
         *  constant type.
         *
         * @param name The name of the variable or parameter.
         * @return The InequalityTerm associated with the given name
         *  in the scope.
         * @exception IllegalActionException If a value in the scope
         * exists with the given name, but cannot be evaluated.
         */
        @Override
        public InequalityTerm getTypeTerm(String name)
                throws IllegalActionException {
            try {
                if (_variables.containsKey(name)) {
                    return null;
                }
                if (!_getTypeForScope(name).equals("parameter")) {
                    throw new IllegalActionException(name + " not a parameter.");
                }
                return _getTypeTermOf(name);
            } catch (PtalonScopeException ex) {
                return null;
            } catch (PtalonRuntimeException ex) {
                return null;
            }
        }

        /** Return a list of names corresponding to the identifiers
         *  defined by this scope. If an identifier is returned in
         *  this list, then get() and getType() will return a value
         *  for the identifier. Note that generally speaking, this
         *  list is extremely expensive to compute, and users should
         *  avoid calling it. It is primarily used for debugging
         *  purposes.
         *
         *  @return A set of identifier names.
         *  @exception IllegalActionException If constructing the list
         *  causes it.
         */
        @Override
        public Set identifierSet() throws IllegalActionException {
            try {
                Set<String> out = _getParameters();
                out.addAll(_variables.keySet());
                return out;
            } catch (PtalonScopeException ex) {
                ex.printStackTrace();
                throw new IllegalActionException("Trouble constructing list: "
                        + ex.getMessage());
            }
        }

        /** Remove the specified variable from this scope.
         *
         *  @param name The name of this variable.
         */
        public void removeVariable(String name) {
            _variables.remove(name);
        }

        /** A map from variables to Tokens.
         */
        private Map<String, Token> _variables = new Hashtable<String, Token>();
    }

    /** FIXME
        @return The next symbol of form "_ifN" where N is 0 if this
        function has not been called and N is n if this is the nth
        call to this function.
     */
    private String _getNextIfSymbol() {
        String symbol = "_if" + _counter;
        _counter++;
        return symbol;
    }

    /** FIXME
     *  @return The parameters in the current scope.
     *  @exception PtalonScopeException If there is any problem
     *  getting the type of a symbol.
     */
    private Set<String> _getParameters() throws PtalonScopeException {
        Set<String> output = new HashSet<String>();
        for (IfTree tree : _currentIfTree.getAncestors()) {
            for (String symbol : tree.getSymbols()) {
                if (tree.getType(symbol).equals("parameter")) {
                    output.add(symbol);
                }
            }
        }
        return output;
    }

    /** Get the type associated with the specified parameter.
     *
     *  @param param The parameter's name in the Ptalon code.
     *  @return Its type.
     *  @exception PtalonRuntimeException If the parameter does not exist.
     */
    private Type _getTypeOf(String param) throws PtalonRuntimeException {
        try {
            String uniqueName = getMappedName(param);
            PtalonParameter att = (PtalonParameter) _actor
                    .getAttribute(uniqueName);
            return att.getType();
        } catch (Exception ex) {
            throw new PtalonRuntimeException("Unable to access int value for "
                    + param, ex);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                        private members                    ////

    /** Get the type term associated with the specified parameter.
     *
     *  @param param The parameter's name in the Ptalon code.
     *  @return Its type.
     *  @exception PtalonRuntimeException If the parameter does not exist.
     */
    private InequalityTerm _getTypeTermOf(String param)
            throws PtalonRuntimeException {
        try {
            String uniqueName = getMappedName(param);
            PtalonParameter att = (PtalonParameter) _actor
                    .getAttribute(uniqueName);
            return att.getTypeTerm();
        } catch (Exception ex) {
            throw new PtalonRuntimeException("Unable to access int value for "
                    + param, ex);
        }
    }

    /** Get the value associated with the specified parameter.
     *
     *  @param param The parameter's name in the Ptalon code.
     *  @return Its unique value.
     *  @exception PtalonRuntimeException If the parameter does not exist.
     */
    private Token _getValueOf(String param) throws PtalonRuntimeException {
        try {
            String uniqueName = getMappedName(param);
            PtalonParameter att = (PtalonParameter) _actor
                    .getAttribute(uniqueName);
            att.toString();
            /*This previous line seems to cause some evaluation that
             * is necessary for the next line to not throw an exception.
             * I don't exactly know why, but things seemed to only work
             * when I was in the debugger, and only when I viewed the "att"
             * value in the debugger.  Since I figured this would require
             * toString() to be called, I tried adding this line, and the
             * exception went away.
             */
            return att.getToken();
        } catch (Exception ex) {
            throw new PtalonRuntimeException("Unable to access int value for "
                    + param, ex);
        }
    }

    /** A counter used to associate a unique number with each if-block.
     */
    private int _counter;

    private boolean _isPreservingTransformation = false;

    /** Whether to reset parameters when the actor is populated and the
     *  parameters already exist for the actor.
     */
    private boolean _resetParameters = true;

    /** The root of the tree containing the symbol tables for each
     *  level of the if-statement hierarchy.
     */
    private IfTree _root;

    ///////////////////////////////////////////////////////////////////
    ////                      protected classes                    ////

    /** _unassignedParameters and _unassignedParameterValues are used
     *  to store parameters which need to be set by Ptalon;
     *  i.e. constant parameters. The first list are the parameters,
     *  and the second list are the expressions to assign to the
     *  parameters.
     */
    private List<String> _unassignedParameterValues = new LinkedList<String>();

    /** _unassignedParameters and _unassignedParameterValues are used
     *  to store parameters which need to be set by Ptalon;
     *  i.e. constant parameters. The first list are the parameters,
     *  and the second list are the expressions to assign to the
     *  parameters.
     */
    private List<PtalonParameter> _unassignedParameters = new LinkedList<PtalonParameter>();
}
