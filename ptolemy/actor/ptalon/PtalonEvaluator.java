/* A code manager that manages the extra complexity of dealing with nested actors.

 Copyright (c) 2006-2014 The Regents of the University of California.
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
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ptolemy.actor.TypedIOPort;
import ptolemy.actor.TypedIORelation;
import ptolemy.actor.parameters.LocationParameter;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.ParseTreeEvaluator;
import ptolemy.data.expr.ParseTreeFreeVariableCollector;
import ptolemy.data.expr.ParseTreeSpecializer;
import ptolemy.data.expr.ParseTreeWriter;
import ptolemy.data.expr.PtParser;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.moml.MoMLParser;
import ptolemy.moml.ParserAttribute;

/**
 * A code manager that manages the extra complexity of dealing with
 * parsing Ptalon actors or values and setting them to parameters of
 * PtalonActors (ones declared in a Ptalon file).
 *
 * FIXME: This implementation could be improved by investigating ways
 * to better address partial evaluation.  A lot of memory gets eaten
 * up by IfTrees, and it would be better to keep only the minimum
 * subset in order to free up memory.
 *
 * @author Adam Cataldo, Elaine Cheong
 * @version $Id$
 * @since Ptolemy II 6.1
 * @Pt.ProposedRating Yellow (celaine)
 * @Pt.AcceptedRating Yellow (celaine)
 *
 */
public class PtalonEvaluator extends AbstractPtalonEvaluator {

    /** Create a new PtalonEvaluator.
     *
     *  @param actor The ptalon actor for this manager.
     */
    public PtalonEvaluator(PtalonActor actor) {
        super(actor);
        _trees = new LinkedList<ActorTree>();
        _instanceNumbers = new Hashtable<String, Integer>();
        _instanceNumbers.put("this", 0);
        try {
            addSymbol("this", "this");
        } catch (PtalonScopeException ex) {
            // This should never happen.
            ex.printStackTrace();
        }
        _currentIfTree.setStatus("this", true);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add an actor to the PtalonActor. In the case of an actor
     *  specified by an import statement, the actor will be a
     *  PtalonActor. In the case of an actor specified by a parameter,
     *  the actor will be arbitrary.
     *  @param name The unique name of the actor declaration.
     *  @exception PtalonRuntimeException If there is any trouble
     *  loading the actor.
     */
    public void addActor(String name) throws PtalonRuntimeException {
        try {
            if (_currentActorTree == null) {
                throw new PtalonRuntimeException("Not in an actor declaration.");
            }

            String symbol = _currentActorTree.getSymbol();

            if (symbol.equals("this")) {
                _currentActorTree.created = true;
                if (_inNewWhileIteration()) {
                    if (_currentIfTree.isForStatement) {
                        _currentActorTree.createdIteration = _currentIfTree.entered;
                    } else {
                        IfTree tree = _currentIfTree;
                        while (!tree.isForStatement) {
                            tree = tree.getParent();
                            if (tree == null) {
                                throw new PtalonRuntimeException(
                                        "In a new for iteration, "
                                                + "but there is no containing "
                                                + "for block.");
                            }
                        }
                        _currentActorTree.createdIteration = tree.entered;
                    }
                } else {
                    _currentActorTree.createdIteration = _currentIfTree.entered;
                }
                _currentActorTree.assignPtalonParameters(_actor);
                _currentActorTree.makeThisConnections();
                _currentActorTree.removeDynamicLeftHandSides();
                return;
            }

            String uniqueName = _actor.uniqueName(symbol);
            if (name != null) {
                if (!inScope(name)) {
                    addSymbol(name, "actor");
                }
                _currentIfTree.mapName(name, uniqueName);
            }

            if (_getType(symbol).equals("import")) {
                PtalonActor actor = new PtalonActor(_actor, uniqueName);
                URL url = _imports.get(symbol);
                actor.ptalonCodeLocation.setToken(new StringToken(url
                        .toString()));
                actor.setNestedDepth(_actor.getNestedDepth() + 1);
                _currentActorTree.assignPtalonParameters(actor);
                _currentActorTree.makeConnections(actor);
                _currentActorTree.removeDynamicLeftHandSides();
            } else if (_getType(symbol).equals("actorparameter")) {
                PtalonParameter parameter = (PtalonParameter) _actor
                        .getAttribute(getMappedName(symbol));
                if (!parameter.hasValue()) {
                    throw new PtalonRuntimeException("Parameter" + symbol
                            + "has no value");
                }
                String expression = parameter.getExpression();
                String[] parsedExpression;
                if (expression.contains("(")) {
                    parsedExpression = _parseActorExpression(expression);
                } else {
                    parsedExpression = new String[1];
                    parsedExpression[0] = expression;
                }
                String actor = parsedExpression[0];
                if (actor.startsWith("ptalonActor:")) {
                    PtalonActor ptalonActor = new PtalonActor(_actor,
                            uniqueName);
                    // Set ptalonCodeLocation to a path to the .ptln file.
                    // The path will have $CLASSPATH in it so that this works
                    // in WebStart and the installer.
                    ptalonActor.ptalonCodeLocation.setToken(new StringToken(
                            _parameterToImport(actor)));
                    ptalonActor.setNestedDepth(_actor.getNestedDepth() + 1);
                    for (int i = 1; i < parsedExpression.length; i = i + 2) {
                        String lhs = parsedExpression[i];
                        String rhs = parsedExpression[i + 1];
                        if (rhs.startsWith("<")) {
                            rhs = rhs.substring(1, rhs.length() - 2);
                        }
                        Parameter param = (Parameter) ptalonActor
                                .getAttribute(lhs);
                        // Use setToken(String) rather than
                        // setExpression(String) because this forces
                        // the parameter to be validated
                        // (attributeChanged() is called and value
                        // dependents are notified).
                        param.setToken(rhs);
                    }
                    _currentActorTree.assignPtalonParameters(ptalonActor);
                    _currentActorTree.makeConnections(ptalonActor);
                    _currentActorTree.removeDynamicLeftHandSides();
                } else {
                    // Actors declared in .ptln code may be defined in
                    // MoML. Using code from
                    // ptolemy.moml.MoMLChangeRequest._execute() to
                    // create a MoMLParser to parse a MoML description
                    // containing a declaration of the actor.

                    // The context in which to execute the request.
                    NamedObj context = _actor;

                    // The MoMLParser.
                    MoMLParser momlParser = null;

                    // Check to see whether there is a parser...

                    //                     if (context != null) {
                    //                         momlParser = ParserAttribute.getParser(context);
                    //                         momlParser.reset();
                    //                     }
                    //
                    //                     if (momlParser == null) {
                    //                         // There is no previously associated parser
                    //                         // (can only happen if context is null).
                    //                         momlParser = new MoMLParser();
                    //                     }
                    //
                    //                     if (context != null) {
                    //                         momlParser.setContext(context);
                    //                     }

                    // FindBugs "Redundant comparison to null
                    // At this point, _actor was already dereferenced,
                    // so it must be non-null
                    momlParser = ParserAttribute.getParser(context);
                    momlParser.reset();
                    momlParser.setContext(context);

                    // MoML description for actor declaration.
                    String description = "<entity name =\"" + uniqueName
                            + "\" class =\"" + actor + "\"/>";
                    momlParser.parse(null, description);

                    ComponentEntity entity = null;
                    // FIXME: reading this code indicates that _actor
                    // is dereferenced above, so it will be non-null.
                    //if (_actor != null) {
                    entity = _actor.getEntity(uniqueName);
                    //}
                    if (/*_actor == null ||*/entity == null) {
                        throw new PtalonRuntimeException(
                                "Could not create new actor.");
                    }

                    for (int i = 1; i < parsedExpression.length; i = i + 2) {
                        String lhs = parsedExpression[i];
                        String rhs = parsedExpression[i + 1];
                        if (rhs.startsWith("<")) {
                            rhs = rhs.substring(1, rhs.length() - 2);
                        }
                        Parameter param = (Parameter) entity.getAttribute(lhs);
                        // Use setToken(String) rather than
                        // setExpression(String) because this forces
                        // the parameter to be validated
                        // (attributeChanged() is called and value
                        // dependents are notified).

                        // FIXME: Will get a null pointer exception if
                        // param is null.
                        param.setToken(rhs);
                    }
                    _currentActorTree.makeConnections(entity);
                    _currentActorTree.assignNonPtalonParameters(entity);
                    _currentActorTree.removeDynamicLeftHandSides();

                    _processAttributes(entity);
                }
                _currentActorTree.created = true;
                if (_inNewWhileIteration()) {
                    if (_currentIfTree.isForStatement) {
                        _currentActorTree.createdIteration = _currentIfTree.entered;
                    } else {
                        IfTree tree = _currentIfTree;
                        while (!tree.isForStatement) {
                            tree = tree.getParent();
                            if (tree == null) {
                                throw new PtalonRuntimeException(
                                        "In a new for iteration, but there "
                                                + "is no containing for block.");
                            }
                        }
                        _currentActorTree.createdIteration = tree.entered;
                    }
                } else {
                    _currentActorTree.createdIteration = _currentIfTree.entered;
                }
            } else { // type of name not "import" or "actorparameter".
                throw new PtalonRuntimeException("Invalid type for " + name);
            }
        } catch (Exception ex) {
            throw new PtalonRuntimeException("Unable to add actor " + name, ex);
        }
    }

    /** Add an assignment of the specified parameter of this actor
     *  declaration to the specified actor declaration. This is not
     *  allowed in nested actor declarations, only top-level
     *  declarations. For instance:
     *
     *  Foo(port := containing) port is okay, but not Bar(a := Foo(port :=
     *  containing))
     *
     *  @param parameterName The name of the parameter.
     *  @param expression The expression to be assigned to the parameter.
     *  @exception PtalonScopeException If this is not a top-level
     *  actor declaration with respect to the assignment, or if
     *  connectPoint is not a port or relation.
     */
    public void addParameterAssign(String parameterName, String expression)
            throws PtalonScopeException {
        if (_currentActorTree == null) {
            throw new PtalonScopeException("Not in an actor declaration");
        }
        _currentActorTree.addParameterAssign(parameterName, expression);
    }

    /** Add an assignment of the specified port of this actor
     *  declaration to the containing Ptalon actor connection point,
     *  which is either a port or a relation. This is not allowed in
     *  nested actor declarations, only top-level declarations. For
     *  instance,
     *
     *  Foo(port := containing) port is okay, but not Bar(a :=
     *  Foo(port := containing))
     *
     * @param portName The name of the port in this
     * @param connectPoint The name of the container's port or relation.
     * @exception PtalonScopeException If this is not a top-level
     * actor declaration with respect to the assignment, or if
     * connectPoint is not a port or relation.
     */
    public void addPortAssign(String portName, String connectPoint)
            throws PtalonScopeException {
        if (_currentActorTree == null) {
            throw new PtalonScopeException("Not in an actor declaration");
        }
        _currentActorTree.addPortAssign(portName, connectPoint);
    }

    /** Add an assignment of the specified port of this actor
     *  declaration to the containing Ptalon actor connection point,
     *  which is either a port or a relation. This is not allowed in
     *  nested actor declarations, only top-level declarations. For
     *  instance,
     *
     *  Foo(port := containing) port is okay, but not Bar(a := Foo(port :=
     *  containing))
     *
     *  @param portName The name of the port in this
     *  @param connectPointPrefix The prefix of the name of the
     *  container's port or relation.
     *  @param connectPointExpression The variable suffix of the name
     *  of the container's port or relation.
     *  @exception PtalonScopeException If this is not a top-level
     *  actor declaration with respect to the assignment, or if
     *  connectPoint is not a port or relation.
     */
    public void addPortAssign(String portName, String connectPointPrefix,
            String connectPointExpression) throws PtalonScopeException {
        _currentActorTree.addPortAssign(portName, connectPointPrefix,
                connectPointExpression);
    }

    /** Add a symbol with the given name and type to the symbol table at the
     *  current level of the if-tree hierarchy.
     *
     *  @param name The symbol name.
     *  @param type The symbol type.
     *  @exception PtalonScopeException If a symbol with this name has
     *  already been added somewhere in the current scope.
     */
    @Override
    public void addSymbol(String name, String type) throws PtalonScopeException {
        super.addSymbol(name, type);
        if (type.equals("actorparameter")) {
            _instanceNumbers.put(name, -1);
        }
    }

    /** Add the unknown left side to this actor declaration.
     *
     *  @param prefix The prefix for the unknown left side.
     *  @param expression The suffix expression for the unknown left side.
     */
    public void addUnknownLeftSide(String prefix, String expression) {
        _currentActorTree.addUnknownLeftSide(prefix, expression);
    }

    /** Enter the named actor declaration.
     *
     *  @param name The name of the actor declaration.
     *  @exception PtalonRuntimeException If such an actor declaration
     *  does not exist.
     */
    public void enterActorDeclaration(String name)
            throws PtalonRuntimeException {
        if (name.equals("this")) {
            return;
        }
        boolean exists = false;
        if (_currentActorTree == null) {
            for (ActorTree tree : _trees) {
                if (tree.getName().equals(name)) {
                    exists = true;
                    _currentActorTree = tree;
                    break;
                }
            }
        } else {
            for (ActorTree tree : _currentActorTree.getChildren()) {
                if (tree.getName().equals(name)) {
                    exists = true;
                    _currentActorTree = tree;
                    break;
                }
            }
        }
        if (!exists) {
            throw new PtalonRuntimeException("Subscope " + name
                    + " does not exist");
        }
    }

    /** Exit the current actor declaration.
     *
     *  @exception PtalonRuntimeException If already at the top-level
     *  if scope.
     */
    public void exitActorDeclaration() throws PtalonRuntimeException {
        if (_currentActorTree == null) {
            throw new PtalonRuntimeException("Already at top level");
        }
        _currentActorTree = _currentActorTree.getParent();
    }

    /** Returns true if the current actor declaration is ready to be
     *  created.
     *
     *  @return true If the current actor declaration is ready to be
     *  created.
     *  @exception PtalonRuntimeException If thrown trying to access a
     *  parameter, or if there is no actor declaration to create.
     */
    public boolean isActorReady() throws PtalonRuntimeException {
        if (_currentActorTree == null) {
            throw new PtalonRuntimeException("No actor to create.");
        }
        if (_currentActorTree.created) {
            if (_inNewWhileIteration()) {
                if (_currentIfTree.isForStatement) {
                    int iteration = _currentActorTree.createdIteration;
                    if (iteration == 0 || iteration == _currentIfTree.entered) {
                        // Just go to the the next thing after the out
                        // if in this case.
                    } else {
                        return false;
                    }
                } else {
                    IfTree tree = _currentIfTree;
                    while (!tree.isForStatement) {
                        tree = tree.getParent();
                        if (tree == null) {
                            throw new PtalonRuntimeException(
                                    "In a new for iteration, "
                                            + "but there is no containing "
                                            + "for block.");
                        }
                    }
                    int iteration = _currentActorTree.createdIteration;
                    if (iteration == 0 || iteration == tree.entered) {
                        // Just go to the the next thing after the out
                        // if in this case.
                    } else {
                        return false;
                    }
                }
                // Coverity says that _currentIfTree cannot be null because _inNewWhileIterator dereferences it.
                //             } else if (_currentIfTree == null) {
                //                 // If we are not in a loop or in an if statement, then we should
                //                 // only check isReady().  -- tfeng
            } else {
                return false;
            }
        }
        if (isReady()) {
            return _currentActorTree.isReady();
        }
        return false;
    }

    /** Pop an actor off of the current tree and return the name.
     *
     *  @return The unique name of the actor declaration being popped
     *  from.
     *  @exception PtalonScopeException If not inside an actor
     *  declaration.
     */
    public String popActorDeclaration() throws PtalonScopeException {
        if (_currentActorTree == null) {
            throw new PtalonScopeException(
                    "Can't pop; not inside nested actor declaration.");
        }
        String output = _currentActorTree.getName();
        _currentActorTree = _currentActorTree.getParent();
        return output;
    }

    /** Push an actor name onto the current tree, or create a new tree if
     *  entering a new nested actor declaration.
     *
     *  @param actorName The name of the actor.
     *  @exception PtalonScopeException If actorName is not a valid
     *  parameter or import in the current scope.
     */
    public void pushActorDeclaration(String actorName)
            throws PtalonScopeException {
        String uniqueName = _uniqueSymbol(actorName);
        if (_currentActorTree == null) {
            _currentActorTree = new ActorTree(null, uniqueName);
            _trees.add(_currentActorTree);
        } else {
            _currentActorTree = _currentActorTree.addChild(uniqueName);
        }
        _currentActorTree.setSymbol(actorName);
    }

    /** Set the parameter name for the current actor declaration, if
     *  any, to the given parameter name.
     *
     *  @param paramName The name of the parameter.
     *  @exception PtalonScopeException If not inside the scope of an
     *  actor declaration.
     */
    public void setActorParameter(String paramName) throws PtalonScopeException {
        if (_currentActorTree == null) {
            throw new PtalonScopeException(
                    "Not inside the scope of an actor declaration.");
        }
        _currentActorTree.setActorParameter(paramName);
    }

    /** Set the symbol in the PtalonCode which represents this
     *  AbstractPtalonEvaluator's actor.
     *
     *  @param symbol The name of this actor in the Ptalon file.
     *  @exception PtalonScopeException If the symbol has been added
     *  already, or if there is some problem accessing its associated
     *  file.
     */
    @Override
    public void setActorSymbol(String symbol) throws PtalonScopeException {
        super.setActorSymbol(symbol);
        _instanceNumbers.put(symbol, -1);
    }

    /** Set whether or not dangling ports are okay. If this input is
     *  false, then dangling ports will be connected to the outside of
     *  this PtalonActor, the default behavior. Setting this to true
     *  means that this is not desired.
     *
     *  @param value true if dangling ports should be left alone.
     */
    public void setDanglingPortsOkay(boolean value) {
        _danglingPortsOkay = value;
    }

    /** Prepare the compiler to start at the outermost scope of the Ptalon
     *  program during run time.
     */
    @Override
    public void startAtTop() {
        super.startAtTop();
        _currentActorTree = null;
    }

    /** The reverse of _importToParameter
     *
     *  @param expression The expression to convert.
     *  @return The converted expression.
     */
    private String _parameterToImport(String expression) {
        // Skip "ptalonActor:"
        return "$CLASSPATH/" + expression.substring(12).replace('.', '/')
                + ".ptln";
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Break an expression like:
     *  <p>
     *  a(x := <1/>, y := <2/>)(z := b(y := <2/>, z := <2/>))
     *  <p>
     *  into an array of expressions like:
     *  <p>
     *  a
     *  <p>
     *  x
     *  <p>
     *  <1/>
     *  <p>
     *  y
     *  <p>
     *  <2/>
     *  <p>
     *  z
     *  <p>
     *  b(y : = <2/>, z := <2/>)
     *
     *  @param expression The expression to split.
     *  @return The array of expression components.
     */
    private String[] _parseActorExpression(String expression) {
        expression = expression.replaceAll("\\)(\\p{Blank})*\\(", ",");
        String[] actorSeparated = expression.split("\\(", 2);
        String actor = actorSeparated[0];
        String remains = actorSeparated[1];
        remains = remains.trim().substring(0, remains.length() - 1);
        LinkedList<Integer> markers = new LinkedList<Integer>();
        int parenthesis = 0;
        for (int i = 0; i < remains.length() - 1; i++) {
            if (remains.charAt(i) == '(') {
                parenthesis++;
            } else if (remains.charAt(i) == ')') {
                parenthesis--;
            } else if (remains.charAt(i) == ',' && parenthesis == 0) {
                markers.add(i);
            }
        }
        String[] assignments = new String[markers.size() + 1];
        int lastMarker = -1;
        int index = 0;
        for (int thisMarker : markers) {
            assignments[index] = remains.substring(lastMarker + 1, thisMarker);
            index++;
            lastMarker = thisMarker;
        }
        assignments[index] = remains
                .substring(lastMarker + 1, remains.length());
        String[] output = new String[2 * assignments.length + 1];
        output[0] = actor;
        for (int i = 0; i < assignments.length; i++) {
            String[] equation = assignments[i].split(":=", 2);
            output[2 * i + 1] = equation[0].trim();
            output[2 * i + 2] = equation[1].trim();
        }
        return output;
    }

    /** Return a unique symbol for the given symbol. The symbol will
     *  always end with a whole number. For instance
     *  _uniqueSymbol("Foo") may return "Foo0", "Foo1", or "Foo2". The
     *  input symbol is assumed to refer to a previously declared
     *  parameter or import statement.
     *
     * @param symbol The symbol from which to derive the unique symbol.
     * @return A unique name.
     * @exception PtalonScopeException If the symbol does not refer to
     * a parameter or import valid in the current scope.
     */
    private String _uniqueSymbol(String symbol) throws PtalonScopeException {
        String type = _getType(symbol);
        if (!(type.equals("import") || type.equals("actorparameter") || type
                .equals("this"))) {
            throw new PtalonScopeException("Symbol " + symbol
                    + " not an import or parameter");
        }
        try {
            Integer number = _instanceNumbers.get(symbol) + 1;
            _instanceNumbers.put(symbol, number);
            String output = "_" + symbol + "_" + number;
            return output;
        } catch (Exception ex) {
            throw new PtalonScopeException("Unable to get unique name for "
                    + symbol, ex);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                        private members                    ////

    /** This represents the current point in the scope of a nested
     *  actor declaration. It is null when not inside an actor
     *  declaration.
     */
    private ActorTree _currentActorTree = null;

    /** If this is true, then dangling, or unconnected ports of actors
     *  contained in the PtalonActor should be left alone. This is
     *  false by default, which means that unconnected ports will be
     *  "brought to the outside" of the PtalonActor.
     */
    private boolean _danglingPortsOkay = false;

    /** This map gives the number for the next instance of the
     *  specified symbol.  As an example, if no instance of the
     *  parameter Foo has been created, and it has created, then
     *  _instanceNumbers.get("Foo") returns 0. If it is created again,
     *  then _instanceNumbers.get("Foo") returns 1.
     */
    private Map<String, Integer> _instanceNumbers;

    /** Each tree in this list represents a nested actor declaration,
     *  like
     *
     *  Foo(a := Foo(a: = Bar(), b := Bar()), b := Bar())
     */
    private List<ActorTree> _trees;

    ///////////////////////////////////////////////////////////////////
    ////                        private classes                    ////

    /** This class is a tree whose structure mimics that of a nested
     *  actor declaration. For instance,
     *
     *  Foo(a := Foo(a: = Bar(), b := Bar()), b := Bar())
     *
     *  might have a tree that looks something like:
     *
     *  <pre>
     *
     *                Foo_0
     *               /     \
     *             Foo_1  Bar_0
     *             /   \
     *          Bar_1  Bar_2
     *  </pre>
     */
    private class ActorTree extends NamedTree<ActorTree> {

        public ActorTree(ActorTree parent, String name) {
            super(parent, name);
            _symbol = name;
        }

        // /////////////////////////////////////////////////////////////////
        // // public methods ////

        /** Create a new child tree to this tree with the specified name and
         *  return it.
         *
         *  @param name The name of the child.
         *  @return The child ActorTree.
         */
        @Override
        public ActorTree addChild(String name) {
            ActorTree tree = new ActorTree(this, name);
            _children.add(tree);
            return tree;
        }

        /** Add an assignment of the specified port of this actor
         *  declaration to the containing Ptalon actor connection
         *  point, which is either a port or a relation. This is not
         *  allowed in nested actor declarations, only top-level
         *  declarations. For instance,
         *
         *  Foo(port := containing)
         *
         *  port is okay, but not
         *
         *  Bar(a := Foo(port := containing))
         *
         *  @param parameterName The name of the parameter.
         *  @param expression The expression to assign to the parameter.
         */
        public void addParameterAssign(String parameterName, String expression) {
            _parameters.put(parameterName, expression);
        }

        /** Add an assignment of the specified port of this actor
         *  declaration to the containing Ptalon actor connection
         *  point, which is either a port or a relation. This is not
         *  allowed in nested actor declarations, only top-level
         *  declarations. For instance,
         *
         *  Foo(port := containing)
         *
         *  port is okay, but not
         *
         *  Bar(a := Foo(port := containing))
         *
         *  If the port on the left does not exist, it will be created
         *  and given a flow type if possible.
         *
         *  @param portName The name of the port in the specified actor.
         *  @param connectPoint The name of the container's port or
         *  relation.
         *  @exception PtalonScopeException If this is not a top-level
         *  actor declaration with respect to the assignment, or if
         *  connectPoint is not a port or relation.
         */
        public void addPortAssign(String portName, String connectPoint)
                throws PtalonScopeException {
            if (_parent != null) {
                throw new PtalonScopeException(
                        "This is not a top-level actor declaration.");
            }
            if (_getType(_symbol).equals("this")) {
                String portType = "";
                boolean transparent = false;
                try {
                    portType = _getType(portName);
                    transparent = true;
                } catch (PtalonScopeException ex) {
                    // FIXME: is it ok to do nothing here?
                    ex.printStackTrace();
                }
                if (transparent && portType.equals("transparent")) {
                    if (_getType(connectPoint).equals("relation")) {
                        _relations.put(portName, connectPoint);
                    } else if (_getType(connectPoint).equals("transparent")) {
                        _transparencies.put(portName, connectPoint);
                    } else if (_getType(connectPoint).endsWith("port")) {
                        addPortAssign(connectPoint, portName);
                        return;
                    } else {
                        throw new PtalonScopeException(connectPoint
                                + " is not a port or relation.");
                    }
                    _transparentLeftHandSides.put(portName, connectPoint);
                    return;
                }
            }
            if (_getType(connectPoint).equals("relation")) {
                _relations.put(portName, connectPoint);
            } else if (_getType(connectPoint).equals("transparent")) {
                _transparencies.put(portName, connectPoint);
            } else if (_getType(connectPoint).endsWith("port")) {
                _ports.put(portName, connectPoint);
            } else {
                throw new PtalonScopeException(connectPoint
                        + " is not a port or relation.");
            }
        }

        /** Add an assignment of the specified port of this actor
         *  declaration to the containing Ptalon actor connection
         *  point, which is either a port or a relation. Here
         *
         *  @param portName The name of the port in this
         *  @param connectPointPrefix The name of the container's port
         *  or relation.
         *  @param connectPointExpression The name of the container's
         *  port or relation.
         *  @exception PtalonScopeException If this is not a top-level
         *  actor declaration with respect to the assignment, or if
         *  connectPoint is not a port or relation.
         */
        public void addPortAssign(String portName, String connectPointPrefix,
                String connectPointExpression) throws PtalonScopeException {
            if (_parent != null) {
                throw new PtalonScopeException(
                        "This is not a top-level actor declaration.");
            }
            _unknownPrefixes.put(portName, connectPointPrefix);
            _unknownExpressions.put(portName, connectPointExpression);
        }

        /** Add the unknown left side to this actor declaration.
         *
         *  @param prefix The prefix for the unknown left side.
         *  @param expression The suffix expression for the unknown
         *  left side.
         */
        public void addUnknownLeftSide(String prefix, String expression) {
            _unknownLeftSides.put(prefix, expression);
        }

        /** Assign all non-Ptalon parameters of the specified
         *  non-Ptalon actor their corresponding value.
         *
         *  @param actor The actor that contains these parameters.
         *  @exception PtalonRuntimeException If thrown trying to
         *  access the parameter, or if unable to set the token for
         *  the corresponding parameter.
         */
        public void assignNonPtalonParameters(ComponentEntity actor)
                throws PtalonRuntimeException {
            try {
                PtParser parser = new PtParser();
                ParseTreeEvaluator _parseTreeEvaluator = new ParseTreeEvaluator();
                for (String parameterName : _parameters.keySet()) {
                    String expression = _parameters.get(parameterName);
                    if (expression == null) {
                        throw new PtalonRuntimeException(
                                "Unable to find expression label for "
                                        + "parameter " + parameterName);
                    }
                    ASTPtRootNode parseTree = parser
                            .generateParseTree(expression);
                    try {
                        Parameter parameter = (Parameter) actor
                                .getAttribute(parameterName);
                        if (parameter == null) {
                            String uniqueName = actor.uniqueName(parameterName);

                            // FIXME: Ptalon assumes that any
                            // parameter we give that is not
                            // predefined in the actor should be an
                            // instance of Parameter. This is not
                            // always the case. Ptalon syntax needs to
                            // be extended to specify new parameters
                            // of a specified class. For now, we
                            // intercept parameters with name
                            // "_location" since these are
                            // particularly useful for graphical
                            // demos.
                            if (uniqueName.equals("_location")) {
                                parameter = new LocationParameter(actor,
                                        uniqueName);
                            } else {
                                parameter = new Parameter(actor, uniqueName);
                            }
                        }
                        try {
                            Token result = _parseTreeEvaluator
                                    .evaluateParseTree(parseTree, _scope);
                            parameter.setToken(result);
                            // We have to validate the parameter so that
                            // value dependents (if any) are notified of
                            // the new value, and so that attributeChanged()
                            // is called on the actor.
                            parameter.validate();
                        } catch (IllegalActionException ex) {
                            ParseTreeFreeVariableCollector collector = new ParseTreeFreeVariableCollector();
                            Set expressionVariables = collector
                                    .collectFreeVariables(parseTree);
                            Set scopeVariables = _scope.identifierSet();
                            List<String> excludedVariables = new LinkedList<String>();
                            for (Object variable : expressionVariables) {
                                if (variable instanceof String) {
                                    if (!scopeVariables.contains(variable)) {
                                        excludedVariables
                                                .add((String) variable);
                                    }
                                }
                            }
                            ParseTreeSpecializer specializer = new ParseTreeSpecializer();
                            parseTree = specializer.specialize(parseTree,
                                    excludedVariables, _scope);
                            ParseTreeWriter writer = new ParseTreeWriter();
                            String outputExpression = writer
                                    .printParseTree(parseTree);
                            parameter.setExpression(outputExpression);
                        }
                    } catch (ClassCastException ex) {
                        Settable parameter = (Settable) actor
                                .getAttribute(parameterName);
                        ParseTreeFreeVariableCollector collector = new ParseTreeFreeVariableCollector();
                        Set expressionVariables = collector
                                .collectFreeVariables(parseTree);
                        Set scopeVariables = _scope.identifierSet();
                        List<String> excludedVariables = new LinkedList<String>();
                        for (Object variable : expressionVariables) {
                            if (variable instanceof String) {
                                if (!scopeVariables.contains(variable)) {
                                    excludedVariables.add((String) variable);
                                }
                            }
                        }
                        ParseTreeSpecializer specializer = new ParseTreeSpecializer();
                        parseTree = specializer.specialize(parseTree,
                                excludedVariables, _scope);
                        ParseTreeWriter writer = new ParseTreeWriter();
                        String outputExpression = writer
                                .printParseTree(parseTree);
                        parameter.setExpression(outputExpression);
                    }
                }
            } catch (Throwable throwable) {
                throw new PtalonRuntimeException("Trouble making connections",
                        throwable);
            }
        }

        /** Assign all Ptalon paramters of the specified actor their
         *  corresponding value.
         *
         *  @param actor The actor that contains these parameters.
         *  @exception PtalonRuntimeException If thrown trying to
         *  access the parameter, or if unable to set the token for
         *  the corresponding parameter.
         */
        public void assignPtalonParameters(PtalonActor actor)
                throws PtalonRuntimeException {
            for (ActorTree child : _children) {
                String paramName = child.getActorParameter();
                PtalonParameter param = actor.getPtalonParameter(paramName);
                try {
                    param.setToken(new StringToken(child.getExpression()));
                } catch (IllegalActionException ex) {
                    throw new PtalonRuntimeException(
                            "Unable to set token for name " + paramName, ex);
                }
            }
            try {
                PtParser parser = new PtParser();
                ParseTreeEvaluator _parseTreeEvaluator = new ParseTreeEvaluator();
                for (String parameterName : _parameters.keySet()) {
                    String expression = _parameters.get(parameterName);
                    if (expression == null) {
                        throw new PtalonRuntimeException(
                                "Unable to find expression label for "
                                        + "parameter " + parameterName);
                    }
                    ASTPtRootNode _parseTree = parser
                            .generateParseTree(expression);
                    Parameter parameter = (Parameter) actor
                            .getAttribute(parameterName);
                    if (parameter == null) {
                        String uniqueName = actor.uniqueName(parameterName);
                        parameter = new PtalonExpressionParameter(actor,
                                uniqueName);
                    }
                    Token result = _parseTreeEvaluator.evaluateParseTree(
                            _parseTree, _scope);
                    parameter.setToken(result);
                    // Validate the parameter to ensure that any value
                    // dependents are notified.
                    parameter.validate();
                }
            } catch (Exception ex) {
                throw new PtalonRuntimeException("Trouble making connections",
                        ex);
            }
        }

        /** Get the name of the actor parameter, or throw an exception
         *  if there is none.
         *
         *  @return The name of the actor parameter.
         *  @exception PtalonRuntimeException If no parameter name has
         *  been assigned to this actor.
         */
        public String getActorParameter() throws PtalonRuntimeException {
            if (_actorParameter == null) {
                throw new PtalonRuntimeException(
                        "Not assigned a parameter name");
            }
            return _actorParameter;
        }

        //        /** Get the first actor tree descendent of this actor tree
        //         *  with the specified name. This should be unique, as each
        //         *  subtree should have a unique name.
        //         *
        //         *  @param uniqueName Unique name of the actor tree to get.
        //         *  @return The descendent, or null if there is none.
        //         */
        //        public ActorTree getActorTree(String uniqueName) {
        //            if (_name.equals(uniqueName)) {
        //                return this;
        //            }
        //            for (ActorTree child : _children) {
        //                if (child.getActorTree(uniqueName) != null) {
        //                    return child.getActorTree(uniqueName);
        //                }
        //            }
        //            return null;
        //        }

        /** Get an expression representing this actor tree, like
         *    a := b(c := d())(n := <2/>)
         *
         *  @return A string containing the expression.
         *  @exception PtalonRuntimeException If no parameter name has
         *  been assigned to this actor.
         */
        public String getExpression() throws PtalonRuntimeException {
            if (_actorParameter == null) {
                throw new PtalonRuntimeException(
                        "Not assigned a parameter name");
            }
            String type = "";
            try {
                type = _getType(_symbol);
            } catch (PtalonScopeException ex) {
                throw new PtalonRuntimeException("Scope Exception", ex);
            }
            StringBuffer buffer = new StringBuffer();
            if (type.equals("import")) {
                buffer.append("ptalonActor:" + _imports.get(_symbol));
            } else if (type.equals("actorparameter")) {
                Parameter parameter = _actor.getPtalonParameter(_symbol);
                buffer.append(parameter.getExpression());
            } else {
                throw new PtalonRuntimeException(
                        "Not assigned a parameter name");
            }
            for (ActorTree child : _children) {
                buffer.append("(" + child.getExpression() + ")");
            }
            for (String param : _parameters.keySet()) {
                buffer.append("(" + param + " := " + _parameters.get(param)
                        + ")");
            }
            String output = buffer.toString();
            return output;
        }

        /** Get the AbstractPtalonEvaluator symbol.
         *
         *  @return The AbstractPtalonEvaluator symbol for this actor
         *  declaration.
         */
        public String getSymbol() {
            return _symbol;
        }

        /** Return true if this nested actor is ready to be created.
         *
         *  @return true If this nested actor is ready to be created.
         *  @exception PtalonRuntimeException If there is problem
         *  accessing any parameters.
         */
        public boolean isReady() throws PtalonRuntimeException {
            for (String portName : _unknownLeftSides.keySet()) {
                String evaluation = evaluateString(_unknownLeftSides
                        .get(portName));
                if (evaluation == null) {
                    return false;
                }
                try {
                    if (_getType(_symbol).equals("this")) {
                        String portType = "";
                        try {
                            portType = _getType(evaluation);
                        } catch (PtalonScopeException ex) {
                            continue;
                        }
                        if (portType.equals("transparent")) {
                            if (!_transparentRelations.containsKey(evaluation)) {
                                return false;
                            }
                        }
                    }
                } catch (PtalonScopeException ex) {
                    throw new PtalonRuntimeException("scope exception", ex);
                }
            }
            for (String portName : _unknownExpressions.keySet()) {
                if (evaluateString(_unknownExpressions.get(portName)) == null) {
                    return false;
                }
            }
            for (String transparency : _transparentLeftHandSides.keySet()) {
                if (!_transparentRelations.containsKey(transparency)) {
                    return false;
                }
                String connectType = "";
                try {
                    connectType = _getType(_transparentLeftHandSides
                            .get(transparency));
                } catch (PtalonScopeException ex) {
                    continue;
                }
                if (connectType.equals("transparent")
                        && !_transparentRelations
                                .containsKey(_transparentLeftHandSides
                                        .get(transparency))) {
                    return false;
                }
            }
            try {
                if (_getType(_symbol).equals("actorparameter")) {
                    PtalonParameter param = _actor.getPtalonParameter(_symbol);
                    if (!param.hasValue()) {
                        return false;
                    }
                } else if (!(_getType(_symbol).equals("import") || _getType(
                        _symbol).equals("this"))) {
                    throw new PtalonRuntimeException("Bad type for symbol "
                            + _symbol);
                }
                for (ActorTree child : _children) {
                    if (!child.isReady()) {
                        return false;
                    }
                }
                return true;
            } catch (Throwable throwable) {
                throw new PtalonRuntimeException(
                        "Unable to check if this actor declaration is ready.",
                        throwable);
            }
        }

        /** Make all connections for this nested actor.
         *
         *  @param actor The actor to connect to others.
         *  @exception PtalonRuntimeException If thrown trying to
         *  access the parameter, or if unable to set the token for
         *  the corresponding parameter.
         */
        public void makeConnections(ComponentEntity actor)
                throws PtalonRuntimeException {
            try {
                for (String portName : _relations.keySet()) {
                    String relationName = _actor.getMappedName(_relations
                            .get(portName));
                    TypedIORelation relation = (TypedIORelation) _actor
                            .getRelation(relationName);
                    TypedIOPort port = (TypedIOPort) actor.getPort(portName);
                    if (port == null) {
                        port = new TypedIOPort(actor,
                                actor.uniqueName(portName));
                        inner: for (Object connection : relation
                                .linkedPortList()) {
                            if (connection instanceof TypedIOPort) {
                                TypedIOPort testPort = (TypedIOPort) connection;
                                if (testPort.getContainer().equals(_actor)) {
                                    if (testPort.isInput()) {
                                        port.setInput(true);
                                    }
                                    if (testPort.isOutput()) {
                                        port.setOutput(true);
                                    }
                                } else {
                                    if (testPort.isInput()) {
                                        port.setOutput(true);
                                    }
                                    if (testPort.isOutput()) {
                                        port.setInput(true);
                                    }
                                }
                                break inner;
                            }
                        }
                    }
                    port.link(relation);
                }
                for (String portName : _transparencies.keySet()) {
                    TypedIOPort port = (TypedIOPort) actor.getPort(portName);
                    String shortName = _transparencies.get(portName);
                    if (_transparentRelations.containsKey(shortName)) {
                        TypedIOPort connectionPoint = _transparentRelations
                                .get(shortName);
                        String relationName = _actor.uniqueName("relation");
                        TypedIORelation rel = new TypedIORelation(_actor,
                                relationName);
                        rel.setWidth(1); // Set explicitly to 1 (the old default) to not break existing models
                        if (port == null) {
                            port = new TypedIOPort(actor,
                                    actor.uniqueName(portName));
                            inner: for (Object connection : rel
                                    .linkedPortList()) {
                                if (connection instanceof TypedIOPort) {
                                    TypedIOPort testPort = (TypedIOPort) connection;
                                    if (testPort.getContainer().equals(_actor)) {
                                        if (testPort.isInput()) {
                                            port.setInput(true);
                                        }
                                        if (testPort.isOutput()) {
                                            port.setOutput(true);
                                        }
                                    } else {
                                        if (testPort.isInput()) {
                                            port.setOutput(true);
                                        }
                                        if (testPort.isOutput()) {
                                            port.setInput(true);
                                        }
                                    }
                                    break inner;
                                }
                            }
                        }
                        port.link(rel);
                        connectionPoint.link(rel);
                    } else {
                        if (port == null) {
                            port = new TypedIOPort(actor,
                                    actor.uniqueName(portName));
                            port.setMultiport(true);
                            port.setInput(true);
                            port.setOutput(false);
                        }
                        _transparentRelations.put(shortName, port);
                    }
                }
                for (String portName : _ports.keySet()) {
                    TypedIOPort port = (TypedIOPort) actor.getPort(portName);
                    String containerPortName = _actor.getMappedName(_ports
                            .get(portName));
                    TypedIOPort containerPort = (TypedIOPort) _actor
                            .getPort(containerPortName);
                    if (port == null) {
                        port = new TypedIOPort(actor,
                                actor.uniqueName(portName));
                        if (containerPort.isInput()) {
                            port.setInput(true);
                        }
                        if (containerPort.isOutput()) {
                            port.setOutput(true);
                        }
                    }
                    String relationName = _actor.uniqueName("relation");
                    TypedIORelation relation = new TypedIORelation(_actor,
                            relationName);
                    relation.setWidth(1); // Set explicitly to 1 (the old default) to not break existing models
                    port.link(relation);
                    containerPort.link(relation);
                }
                for (String portName : _unknownPrefixes.keySet()) {
                    String suffix = evaluateString(_unknownExpressions
                            .get(portName));
                    if (suffix == null) {
                        throw new PtalonRuntimeException(
                                "Not able to evaluate suffix "
                                        + _unknownExpressions.get(portName));
                    }
                    String name = _unknownPrefixes.get(portName) + suffix;
                    if (_getType(name).endsWith("port")) {
                        TypedIOPort port = (TypedIOPort) actor
                                .getPort(portName);
                        String containerPortName = _actor.getMappedName(name);
                        TypedIOPort containerPort = (TypedIOPort) _actor
                                .getPort(containerPortName);
                        if (port == null) {
                            port = new TypedIOPort(actor,
                                    actor.uniqueName(portName));
                            if (containerPort.isInput()) {
                                port.setInput(true);
                            }
                            if (containerPort.isOutput()) {
                                port.setOutput(true);
                            }
                        }
                        String relationName = _actor.uniqueName("relation");
                        TypedIORelation relation = new TypedIORelation(_actor,
                                relationName);
                        relation.setWidth(1); // Set explicitly to 1 (the old default) to not break existing models
                        port.link(relation);
                        containerPort.link(relation);
                    } else if (_getType(name).equals("relation")) {
                        String relationName = _actor.getMappedName(name);
                        TypedIORelation relation = (TypedIORelation) _actor
                                .getRelation(relationName);
                        TypedIOPort port = (TypedIOPort) actor
                                .getPort(portName);
                        if (port == null) {
                            port = new TypedIOPort(actor,
                                    actor.uniqueName(portName));
                            inner: for (Object connection : relation
                                    .linkedPortList()) {
                                if (connection instanceof TypedIOPort) {
                                    TypedIOPort testPort = (TypedIOPort) connection;
                                    if (testPort.getContainer().equals(_actor)) {
                                        if (testPort.isInput()) {
                                            port.setInput(true);
                                        }
                                        if (testPort.isOutput()) {
                                            port.setOutput(true);
                                        }
                                    } else {
                                        if (testPort.isInput()) {
                                            port.setOutput(true);
                                        }
                                        if (testPort.isOutput()) {
                                            port.setInput(true);
                                        }
                                    }
                                    break inner;
                                }
                            }
                        }
                        port.link(relation);
                    } else if (_getType(name).equals("transparent")) {
                        TypedIOPort port = (TypedIOPort) actor
                                .getPort(portName);
                        if (_transparentRelations.containsKey(name)) {
                            TypedIOPort connectionPoint = _transparentRelations
                                    .get(name);
                            String relationName = _actor.uniqueName("relation");
                            TypedIORelation rel = new TypedIORelation(_actor,
                                    relationName);
                            rel.setWidth(1); // Set explicitly to 1 (the old default) to not break existing models
                            if (port == null) {
                                port = new TypedIOPort(actor,
                                        actor.uniqueName(portName));
                                inner: for (Object connection : rel
                                        .linkedPortList()) {
                                    if (connection instanceof TypedIOPort) {
                                        TypedIOPort testPort = (TypedIOPort) connection;
                                        if (testPort.getContainer().equals(
                                                _actor)) {
                                            if (testPort.isInput()) {
                                                port.setInput(true);
                                            }
                                            if (testPort.isOutput()) {
                                                port.setOutput(true);
                                            }
                                        } else {
                                            if (testPort.isInput()) {
                                                port.setOutput(true);
                                            }
                                            if (testPort.isOutput()) {
                                                port.setInput(true);
                                            }
                                        }
                                        break inner;
                                    }
                                }
                            }
                            port.link(rel);
                            connectionPoint.link(rel);
                        } else {
                            if (port == null) {
                                port = new TypedIOPort(actor,
                                        actor.uniqueName(portName));
                                port.setMultiport(true);
                                port.setInput(true);
                                port.setOutput(false);
                            }
                            _transparentRelations.put(name, port);
                        }
                    } else {
                        throw new PtalonRuntimeException(name
                                + " not a port or relation");
                    }
                }
                if (_danglingPortsOkay) {
                    return;
                }
                PtalonActor container = (PtalonActor) actor.getContainer();
                for (Object port : actor.portList()) {
                    if (port instanceof TypedIOPort) {
                        TypedIOPort ioport = (TypedIOPort) port;
                        if (ioport.numLinks() == 0) {
                            String name = container.uniqueName(actor.getName()
                                    + "_" + ioport.getName());
                            TypedIOPort newPort = new TypedIOPort(container,
                                    name);
                            String rel = container.uniqueName("relation");
                            TypedIORelation relation = new TypedIORelation(
                                    container, rel);
                            relation.setWidth(1); // Set explicitly to 1 (the old default) to not break existing models
                            if (ioport.isMultiport()) {
                                relation.setWidth(ioport.getWidth());
                                newPort.setMultiport(true);
                                if (!ioport.isOutsideConnected()) {
                                    ioport.link(relation);
                                    newPort.link(relation);
                                } else {
                                    int width = ioport.getWidth();
                                    while (width > 0) {
                                        ioport.link(relation);
                                        newPort.link(relation);
                                        width--;
                                    }
                                }
                            } else {
                                ioport.link(relation);
                                newPort.link(relation);
                            }
                        }
                    }
                }
            } catch (Throwable throwable) {
                throw new PtalonRuntimeException("Trouble making connections",
                        throwable);
            }
        }

        /** Make all connections for this nested actor.
         *
         *  @exception PtalonRuntimeException If thrown trying to
         *  access the parameter, or if unable to set the token for
         *  the corresponding parameter.
         */
        public void makeThisConnections() throws PtalonRuntimeException {
            try {
                for (String portName : _relations.keySet()) {
                    String relationName = _actor.getMappedName(_relations
                            .get(portName));
                    TypedIORelation relation = (TypedIORelation) _actor
                            .getRelation(relationName);
                    TypedIOPort port = (TypedIOPort) _actor.getPort(portName);
                    if (port == null) {
                        if (_transparentRelations.containsKey(portName)) {
                            port = _transparentRelations.get(portName);
                        } else {
                            throw new PtalonRuntimeException("No port named "
                                    + portName);
                        }
                    }
                    port.link(relation);
                }
                for (String portName : _transparencies.keySet()) {
                    TypedIOPort port = (TypedIOPort) _actor.getPort(portName);
                    String shortName = _transparencies.get(portName);
                    if (_transparentRelations.containsKey(shortName)) {
                        TypedIOPort connectionPoint = _transparentRelations
                                .get(shortName);
                        String relationName = _actor.uniqueName("relation");
                        TypedIORelation rel = new TypedIORelation(_actor,
                                relationName);
                        rel.setWidth(1); // Set explicitly to 1 (the old default) to not break existing models
                        if (port == null) {
                            if (_transparentRelations.containsKey(portName)) {
                                port = _transparentRelations.get(portName);
                            } else {
                                throw new PtalonRuntimeException(
                                        "No port named " + portName);
                            }
                        }
                        port.link(rel);
                        connectionPoint.link(rel);
                    } else {
                        if (port == null) {
                            throw new PtalonRuntimeException("No port named "
                                    + portName);
                        }
                        _transparentRelations.put(shortName, port);
                    }
                }
                for (String portName : _ports.keySet()) {
                    TypedIOPort port = (TypedIOPort) _actor.getPort(portName);
                    String containerPortName = _actor.getMappedName(_ports
                            .get(portName));
                    TypedIOPort containerPort = (TypedIOPort) _actor
                            .getPort(containerPortName);
                    if (port == null) {
                        throw new PtalonRuntimeException("No port named "
                                + portName);
                    }
                    String relationName = _actor.uniqueName("relation");
                    TypedIORelation relation = new TypedIORelation(_actor,
                            relationName);
                    relation.setWidth(1); // Set explicitly to 1 (the old default) to not break existing models
                    port.link(relation);
                    containerPort.link(relation);
                }
                for (String portName : _unknownPrefixes.keySet()) {
                    String suffix = evaluateString(_unknownExpressions
                            .get(portName));
                    if (suffix == null) {
                        throw new PtalonRuntimeException(
                                "Not able to evaluate suffix "
                                        + _unknownExpressions.get(portName));
                    }
                    String name = _unknownPrefixes.get(portName) + suffix;
                    if (_getType(name).endsWith("port")) {
                        TypedIOPort port = (TypedIOPort) _actor
                                .getPort(portName);
                        String containerPortName = _actor.getMappedName(name);
                        TypedIOPort containerPort = (TypedIOPort) _actor
                                .getPort(containerPortName);
                        if (port == null) {
                            throw new PtalonRuntimeException("No port named "
                                    + portName);
                        }
                        String relationName = _actor.uniqueName("relation");
                        TypedIORelation relation = new TypedIORelation(_actor,
                                relationName);
                        relation.setWidth(1); // Set explicitly to 1 (the old default) to not break existing models
                        port.link(relation);
                        containerPort.link(relation);
                    } else if (_getType(name).equals("relation")) {
                        String relationName = _actor.getMappedName(name);
                        TypedIORelation relation = (TypedIORelation) _actor
                                .getRelation(relationName);
                        TypedIOPort port = (TypedIOPort) _actor
                                .getPort(portName);
                        if (port == null) {
                            if (_transparentRelations.containsKey(portName)) {
                                port = _transparentRelations.get(portName);
                            } else {
                                throw new PtalonRuntimeException(
                                        "No port named " + portName);
                            }
                        }
                        port.link(relation);
                    } else if (_getType(name).equals("transparent")) {
                        TypedIOPort port = (TypedIOPort) _actor
                                .getPort(portName);
                        if (_transparentRelations.containsKey(name)) {
                            TypedIOPort connectionPoint = _transparentRelations
                                    .get(name);
                            String relationName = _actor.uniqueName("relation");
                            TypedIORelation rel = new TypedIORelation(_actor,
                                    relationName);
                            rel.setWidth(1); // Set explicitly to 1 (the old default) to not break existing models
                            if (port == null) {
                                if (_transparentRelations.containsKey(portName)) {
                                    port = _transparentRelations.get(portName);
                                } else {
                                    throw new PtalonRuntimeException(
                                            "No port named " + portName);
                                }
                            }
                            port.link(rel);
                            connectionPoint.link(rel);
                        } else {
                            if (port == null) {
                                throw new PtalonRuntimeException(
                                        "No port named " + portName);
                            }
                            _transparentRelations.put(name, port);
                        }
                    } else {
                        throw new PtalonRuntimeException(name
                                + " not a port or relation");
                    }
                }
            } catch (Throwable throwable) {
                throw new PtalonRuntimeException("Trouble making connections",
                        throwable);
            }
        }

        /** Clean up any dynamic left hand sides added.
         */
        public void removeDynamicLeftHandSides() {
            for (String prefix : _unknownLeftSides.keySet()) {
                String suffix = evaluateString(_unknownLeftSides.get(prefix));
                if (suffix == null) {
                    continue;
                }
                String name = prefix + suffix;
                if (_parameters.containsKey(name)) {
                    _parameters.remove(name);
                }
                if (_ports.containsKey(name)) {
                    _parameters.remove(name);
                }
                if (_relations.containsKey(name)) {
                    _relations.remove(name);
                }
                if (_transparencies.containsKey(name)) {
                    _transparencies.remove(name);
                }
                if (_unknownExpressions.containsKey(name)) {
                    _unknownExpressions.remove(name);
                }
                if (_unknownPrefixes.containsKey(name)) {
                    _unknownPrefixes.remove(name);
                }
            }
        }

        /** Set the parameter name for the current actor declaration,
         *  if any, to the given parameter name.
         *
         *  @param paramName The name of the parameter.
         */
        public void setActorParameter(String paramName)
                throws PtalonScopeException {
            _actorParameter = paramName;
        }

        /** Set the symbol in the AbstractPtalonEvaluator this actor
         *  declaration refers to.  It should have type
         *  "actorparameter" or "import".
         *
         *  @param symbol The symbol to set.
         */
        public void setSymbol(String symbol) {
            _symbol = symbol;
        }

        //        /** Write an xml version of this actor to the given output.
        //         *
        //         *  @param output The writer to which to send the output.
        //         *  @param depth The depth of indents with which to start.
        //         *  @exception IOException If there is a problem writing to
        //         *  the output.
        //         */
        //        public void xmlSerialize(Writer output, int depth) throws IOException {
        //            String text;
        //            if (_actorParameter == null) {
        //                text = _getIndentPrefix(depth) + "<actor_declaration name=\""
        //                        + _name + "\" symbol=\"" + _symbol + "\">\n";
        //            } else {
        //                text = _getIndentPrefix(depth) + "<actor_declaration name=\""
        //                        + _name + "\" actorParameter=\"" + _actorParameter
        //                        + "\" symbol=\"" + _symbol + "\">\n";
        //            }
        //            output.write(text);
        //            for (ActorTree child : _children) {
        //                child.xmlSerialize(output, depth + 1);
        //            }
        //            output.write(_getIndentPrefix(depth) + "</actor_declaration>\n");
        //        }

        ///////////////////////////////////////////////////////////////////
        ////                        public members                    ////

        /** This becomes true after the this actor declaration has
         *  been created.
         */
        public boolean created = false;

        /** This takes a nonzero value in the iteration it gets set.
         */
        public int createdIteration = 0;

        // /////////////////////////////////////////////////////////////////
        // // private members ////

        /** The left hand side in the Ptalon expression param := ...
         */
        private String _actorParameter = null;

        /** Each key is a parameter in this actor declaration, and
         *  each value is an expression to be passed to the parameter.
         */
        private Map<String, String> _parameters = new Hashtable<String, String>();

        /** Each key is a port in this actor declaration, and each value is a
         *  port in its container to be connected to at runtime.
         */
        private Map<String, String> _ports = new Hashtable<String, String>();

        /** Each key is a port in this actor declaration, and each value is a
         *  relation in its container to be connected to at runtime.
         */
        private Map<String, String> _relations = new Hashtable<String, String>();

        /** This is the symbol stored with the AbstractPtalonEvaluator that this
         *  actor declaration refers to. It's either a
         *  "actorparameter", "import", or "this" symbol.
         */
        private String _symbol;

        /** Each key is a port in this actor declaration, and each
         *  value is a transparent relation in its container to be
         *  connected to at runtime.
         */
        private Map<String, String> _transparencies = new Hashtable<String, String>();

        /** Each member of this set is a transparent relation assigned
         *  a value in a this statement, like
         *    this(transparentRelation := someOtherRelation);
         */
        private Map<String, String> _transparentLeftHandSides = new Hashtable<String, String>();

        /** The _unknownPrefixes maps port names in this actor
         *  declaration instance to prefixes of unknown connection
         *  points, and _unknownExpressions maps the same keys to the
         *  expressions for these unknown connection points.
         */
        private Map<String, String> _unknownExpressions = new Hashtable<String, String>();

        /** Each key is a prefix and value is an expression
         *  corresponding to a left hand side of an assignment which
         *  may change dynamically.
         */
        private Map<String, String> _unknownLeftSides = new Hashtable<String, String>();

        /** The _unknownPrefixes maps port names in this actor
         *  declaration instance to prefixes of unknown connection
         *  points, and _unknownExpressions maps the same keys to the
         *  expressions for these unknown connection points.
         */
        private Map<String, String> _unknownPrefixes = new Hashtable<String, String>();

    }
}
