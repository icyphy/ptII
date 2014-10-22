/* Superclass of the special parameters used in transformation rules.

@Copyright (c) 2007-2014 The Regents of the University of California.
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

package ptolemy.actor.gt;

import java.util.HashSet;
import java.util.Set;

import ptolemy.actor.gt.data.MatchResult;
import ptolemy.data.ObjectToken;
import ptolemy.data.Token;
import ptolemy.data.expr.ASTPtMethodCallNode;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.ParseTreeEvaluator;
import ptolemy.data.expr.ParseTreeTypeInference;
import ptolemy.data.expr.ParserScope;
import ptolemy.data.type.ObjectType;
import ptolemy.data.type.Type;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// GTParameter

/**
 Superclass of the special parameters used in transformation rules.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating Yellow (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class GTParameter extends Parameter {

    /** Construct a parameter with the given name contained by the specified
     *  entity. The container argument must not be null, or a
     *  NullPointerException will be thrown.  This parameter will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  The object is not added to the list of objects in the workspace
     *  unless the container is null.
     *  Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of the parameter.
     *  @exception IllegalActionException If the parameter is not of an
     *   acceptable class for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   a parameter already in the container.
     */
    public GTParameter(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    //// Evaluator

    /**
     The evaluator used in a transformation rule. It differs from other
     evaluators in that it uses a pattern and a match result to resolve names
     in both the pattern and the host model that the pattern matches in the
     match result. If a name is found in the host model, its value in the host
     model is returned, regardless of whether the same name exists in the
     pattern or not (even if this evaluator is used to evaluate a parameter
     specified in the pattern).

     @author Thomas Huining Feng
     @version $Id$
     @since Ptolemy II 7.1
     @Pt.ProposedRating Yellow (tfeng)
     @Pt.AcceptedRating Red (tfeng)
     */
    public static class Evaluator extends ParseTreeEvaluator {

        /** Construct an evaluator.
         *
         *  @param pattern The pattern.
         *  @param matchResult The match result for the match between the
         *   pattern and a host model.
         */
        public Evaluator(Pattern pattern, MatchResult matchResult) {
            _pattern = pattern;
            _matchResult = matchResult;
            _typeInference = new TypeInference(pattern, matchResult);
        }

        /** Evaluate the parse tree with a scope that can resolve more names
         *  than the given scope. The used scope first searches for names in the
         *  host model. If the attempt fails, it invokes the given scope to
         *  resolve the names.
         *
         *  @param node The node to be evaluated.
         *  @param scope The scope to be used as a fallback.
         *  @return The result of evaluation.
         *  @exception IllegalActionException If an error occurs during
         *   evaluation.
         */
        @Override
        public Token evaluateParseTree(ASTPtRootNode node, ParserScope scope)
                throws IllegalActionException {
            return super.evaluateParseTree(node,
                    _createScope(_pattern, _matchResult, scope));
        }

        /** Apply a method to the children of the specified node, where the
         *  first child is the object on which the method is defined and the
         *  rest of the children are arguments. This also handles indexing into
         *  a record, which looks the same.
         *  @param node The specified node.
         *  @exception IllegalActionException If an evaluation error occurs.
         */
        @Override
        public void visitMethodCallNode(ASTPtMethodCallNode node)
                throws IllegalActionException {
            int argCount = node.jjtGetNumChildren();
            if (argCount == 1) {
                Token firstChild = _evaluateChild(node, 0);
                if (firstChild instanceof ObjectToken) {
                    ObjectToken objectToken = (ObjectToken) firstChild;
                    Object object = objectToken.getValue();
                    if (object instanceof NamedObj) {
                        NamedObj patternObject = (NamedObj) _matchResult
                                .getKey(object);
                        if (patternObject != null) {
                            String methodName = node.getMethodName();
                            NamedObj patternChild = GTTools.getChild(
                                    patternObject, methodName, true, true,
                                    true, true);
                            if (patternChild != null
                                    && _matchResult.containsKey(patternChild)) {
                                Object hostChild = _matchResult
                                        .get(patternChild);
                                _evaluatedChildToken = new ObjectToken(
                                        hostChild, hostChild.getClass());
                                return;
                            }
                        }
                    }
                }
            }

            super.visitMethodCallNode(node);
        }

        /** Create the scope to be used for name resolution.
         *
         *  @param pattern The pattern.
         *  @param matchResult The match result.
         *  @param superScope The scope to fall back on if a name cannot be
         *   resolved.
         *  @return The scope.
         */
        protected ParserScope _createScope(Pattern pattern,
                MatchResult matchResult, ParserScope superScope) {
            return new Scope(pattern, matchResult, superScope);
        }

        /** The match result.
         */
        private MatchResult _matchResult;

        /** The pattern.
         */
        private Pattern _pattern;
    }

    ///////////////////////////////////////////////////////////////////
    //// Scope

    /**
     A scope to be used in {@link Evaluator} to resolve names with a pattern and
     a match result.

     @author Thomas Huining Feng
     @version $Id$
     @since Ptolemy II 7.1
     @Pt.ProposedRating Yellow (tfeng)
     @Pt.AcceptedRating Red (tfeng)
     */
    public static class Scope implements ParserScope {

        /** Construct a scope.
         *
         *  @param pattern The pattern.
         *  @param matchResult The match result for the match between the
         *   pattern and a host model.
         *  @param superscope The scope to fall back on if a name cannot be
         *   resolved.
         */
        public Scope(Pattern pattern, MatchResult matchResult,
                ParserScope superscope) {
            _pattern = pattern;
            _matchResult = matchResult;
            _superscope = superscope;
        }

        /** Look up and return the value with the specified name in the
         *  scope. Return null if the name is not defined in this scope.
         *  @param name The name of the variable to be looked up.
         *  @return The token associated with the given name in the scope.
         *  @exception IllegalActionException If a value in the scope
         *  exists with the given name, but cannot be evaluated.
         */
        @Override
        public Token get(String name) throws IllegalActionException {
            // Resolve ports, entities and relations in the pattern.
            NamedObj patternChild = GTTools.getChild(_pattern, name, false,
                    true, true, true);
            if (patternChild != null && _matchResult.containsKey(patternChild)) {
                // If found and there is a match (patternChild has not been
                // ignored), return the matching object in the host.
                NamedObj child = (NamedObj) _matchResult.get(patternChild);
                return new ObjectToken(child, child.getClass());
            } else {
                // If not, get from the superscope.
                Token token = _superscope.get(name);
                if (token == null) {
                    // If not found, look for parameters in the entities that
                    // contains the pattern (e.g., the transformation rule or
                    // the model that encloses the transformation rule).
                    NamedObj container = _pattern.getContainer();
                    if (container != null) {
                        NamedObjVariable containerVar = NamedObjVariable
                                .getNamedObjVariable(container, true);
                        ParserScope containerScope = containerVar
                                .getParserScope();
                        token = containerScope.get(name);
                        return token;
                    }
                }
                if (token instanceof ObjectToken) {
                    Object value = ((ObjectToken) token).getValue();
                    if (value instanceof Port || value instanceof Entity
                            || value instanceof Relation) {
                        // If the superscope returns an ObjectToken containing a
                        // port, an entity, or a relation, do not return it
                        // because we don't want this object in the pattern to
                        // be referred to in a constraint.
                        return ObjectToken.NULL;
                    }
                }
                return token;
            }
        }

        /** Look up and return the type of the value with the specified
         *  name in the scope. Return null if the name is not defined in
         *  this scope.
         *  @param name The name of the variable to be looked up.
         *  @return The token associated with the given name in the scope.
         *  @exception IllegalActionException If a value in the scope
         *  exists with the given name, but cannot be evaluated.
         */
        @Override
        public Type getType(String name) throws IllegalActionException {
            Token token = get(name);
            if (token != null) {
                return token.getType();
            } else {
                return _superscope.getType(name);
            }
        }

        /** Look up and return the type term for the specified name
         *  in the scope. Return null if the name is not defined in this
         *  scope, or is a constant type.
         *  @param name The name of the variable to be looked up.
         *  @return The InequalityTerm associated with the given name in
         *  the scope.
         *  @exception IllegalActionException If a value in the scope
         *  exists with the given name, but cannot be evaluated.
         */
        @Override
        public InequalityTerm getTypeTerm(String name)
                throws IllegalActionException {
            return _superscope.getTypeTerm(name);
        }

        /** Return a list of names corresponding to the identifiers
         *  defined by this scope.  If an identifier is returned in this
         *  list, then get() and getType() will return a value for the
         *  identifier.  Note that generally speaking, this list is
         *  extremely expensive to compute, and users should avoid calling
         *  it.  It is primarily used for debugging purposes.
         *  @return A list of names corresponding to the identifiers
         *  defined by this scope.
         *  @exception IllegalActionException If constructing the list causes
         *  it.
         */
        @Override
        public Set<?> identifierSet() throws IllegalActionException {
            Set<Object> identifiers = new HashSet<Object>(
                    _superscope.identifierSet());
            try {
                _pattern.workspace().getReadAccess();
                for (Object childObject : GTTools.getChildren(_pattern, true,
                        true, true, true)) {
                    identifiers.add(((NamedObj) childObject).getName());
                }
            } finally {
                _pattern.workspace().doneReading();
            }
            return identifiers;
        }

        /** The match result.
         */
        protected MatchResult _matchResult;

        /** The pattern.
         */
        protected Pattern _pattern;

        /** The scope used to resolve a name if it cannot be resolved in this
         *  scope.
         */
        protected ParserScope _superscope;
    }

    ///////////////////////////////////////////////////////////////////
    //// TypeInference

    /**
     The type inference used to infer types of names in the host model and in
     the pattern, which is used in {@link Evaluator}.

     @author Thomas Huining Feng
     @version $Id$
     @since Ptolemy II 7.1
     @Pt.ProposedRating Yellow (tfeng)
     @Pt.AcceptedRating Red (tfeng)
     */
    public static class TypeInference extends ParseTreeTypeInference {

        /** Construct a type inference.
         *
         *  @param pattern The pattern.
         *  @param matchResult The match result for the match between the
         *   pattern and a host model.
         */
        public TypeInference(Pattern pattern, MatchResult matchResult) {
            _pattern = pattern;
            _matchResult = matchResult;
        }

        /** Infer the type of the parse tree with the specified root node using
         *  the specified scope to resolve the values of variables.
         *  @param node The root of the parse tree.
         *  @param scope The scope for evaluation.
         *  @return The result of evaluation.
         *  @exception IllegalActionException If an error occurs during
         *   evaluation.
         */
        @Override
        public Type inferTypes(ASTPtRootNode node, ParserScope scope)
                throws IllegalActionException {
            return super.inferTypes(node, new Scope(_pattern, _matchResult,
                    scope));
        }

        /** Set the type of the given node to be the return type of the
         *  method determined for the given node.
         *  @param node The specified node.
         *  @exception IllegalActionException If an inference error occurs.
         */
        @Override
        public void visitMethodCallNode(ASTPtMethodCallNode node)
                throws IllegalActionException {
            int argCount = node.jjtGetNumChildren();
            if (argCount == 1) {
                Type firstChild = super._inferChild(node, 0);
                if (firstChild instanceof ObjectType) {
                    ObjectType objectType = (ObjectType) firstChild;
                    Object object = objectType.getValue();
                    if (object instanceof NamedObj) {
                        NamedObj patternObject = (NamedObj) _matchResult
                                .getKey(object);
                        if (patternObject != null) {
                            String methodName = node.getMethodName();
                            NamedObj patternChild = GTTools.getChild(
                                    patternObject, methodName, true, true,
                                    true, true);
                            if (patternChild != null
                                    && _matchResult.containsKey(patternChild)) {
                                Object hostChild = _matchResult
                                        .get(patternChild);
                                _setType(node, new ObjectType(hostChild,
                                        hostChild.getClass()));
                                return;
                            }
                        }
                    }
                }
            }

            super.visitMethodCallNode(node);
        }

        /** The match result.
         */
        private MatchResult _matchResult;

        /** The pattern.
         */
        private Pattern _pattern;
    }

    /** Evaluate the current expression to a token with the given pattern and
     *  match result using {@link Evaluator}. If this variable
     *  was last set directly with a token, then do nothing. In other words,
     *  the expression is evaluated only if the value of the token was most
     *  recently given by an expression.  The expression is also evaluated
     *  if any of the variables it refers to have changed since the last
     *  evaluation.  If the value of this variable
     *  changes due to this evaluation, then notify all
     *  value dependents and notify the container (if there is one) by
     *  calling its attributeChanged() and attributeTypeChanged() methods,
     *  as appropriate. An exception is thrown
     *  if the expression is illegal, for example if a parse error occurs
     *  or if there is a dependency loop.
     *  <p>
     *  If evaluation results in a token that is not of the same type
     *  as the current type of the variable, then the type of the variable
     *  is changed, unless the new type is incompatible with statically
     *  specified types (setTypeEquals() and setTypeAtMost()).
     *  If the type is changed, the attributeTypeChanged() method of
     *  the container is called.  The container can reject the change
     *  by throwing an exception.
     *  <p>
     *  This method may trigger a model error, which is delegated up
     *  the container hierarchy until an error handler is found, and
     *  is ignored if no error handler is found.  A model error occurs
     *  if the expression cannot be parsed or cannot be evaluated.
     *  <p>
     *  Part of this method is read-synchronized on the workspace.
     *
     *  @param pattern The pattern.
     *  @param matchResult The match result for the match between the pattern
     *   and a host model.
     *  @exception IllegalActionException If the expression cannot
     *   be parsed or cannot be evaluated, or if a dependency loop is found.
     */
    protected void _evaluate(Pattern pattern, MatchResult matchResult)
            throws IllegalActionException {
        setParseTreeEvaluator(new Evaluator(pattern, matchResult));
        try {
            super._evaluate();
        } finally {
            setParseTreeEvaluator(null);
            _parserScope = null;
        }
    }
}
