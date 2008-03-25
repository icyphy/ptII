/*

@Copyright (c) 2007-2008 The Regents of the University of California.
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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import ptolemy.actor.gt.data.MatchResult;
import ptolemy.data.BooleanToken;
import ptolemy.data.Function;
import ptolemy.data.FunctionToken;
import ptolemy.data.ObjectToken;
import ptolemy.data.Token;
import ptolemy.data.expr.ASTPtFunctionApplicationNode;
import ptolemy.data.expr.ASTPtFunctionalIfNode;
import ptolemy.data.expr.ASTPtLeafNode;
import ptolemy.data.expr.ASTPtMethodCallNode;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.ConversionUtilities;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.ParseTreeEvaluator;
import ptolemy.data.expr.ParserScope;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.FunctionType;
import ptolemy.data.type.Type;
import ptolemy.graph.InequalityTerm;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

/**

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class GTParameter extends Parameter {

    /**
     * @param container
     * @param name
     * @exception IllegalActionException
     * @exception NameDuplicationException
     */
    public GTParameter(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    public static class Evaluator extends ParseTreeEvaluator {

        public Evaluator(Pattern pattern, MatchResult matchResult) {
            _pattern = pattern;
            _matchResult = matchResult;
        }

        public Token evaluateParseTree(ASTPtRootNode node, ParserScope scope)
                throws IllegalActionException {
            return super.evaluateParseTree(node, new Scope(_pattern,
                    _matchResult, scope));
        }

        public void visitFunctionApplicationNode(
                ASTPtFunctionApplicationNode node)
        throws IllegalActionException {
            String functionName = node.getFunctionName();
            if (functionName != null && functionName.equals("loop")) {
                if (node.jjtGetNumChildren() != 5) {
                    throw new IllegalActionException("The loop special " +
                            "function requires exactly 4 arguments.");
                }

                String current = null;
                ASTPtRootNode child = (ASTPtRootNode) node.jjtGetChild(1);
                if (child != null && child instanceof ASTPtLeafNode &&
                        ((ASTPtLeafNode) child).isIdentifier()) {
                    current = ((ASTPtLeafNode) child).getName();
                }
                if (current == null) {
                    throw new IllegalActionException("The first argument to " +
                            "the loop special function must be an identifier.");
                }

                Token startValue = _evaluateChild(node, 2);

                String boundVariable = null;
                child = (ASTPtRootNode) node.jjtGetChild(3);
                if (child != null && child instanceof ASTPtLeafNode) {
                    boundVariable = ((ASTPtLeafNode) child).getName();
                }
                if (boundVariable == null) {
                    throw new IllegalActionException("The third argument to " +
                            "the loop special function must be an identifier.");
                }

                Collection<?> collection = null;
                Token token = _evaluateChild(node, 4);
                if (token instanceof ObjectToken && ((ObjectToken) token)
                        .getValue() instanceof Collection) {
                    collection =
                        (Collection<?>) ((ObjectToken) token).getValue();
                }
                if (collection == null) {
                    throw new IllegalActionException("The forth argument to " +
                            "the loop special function must be a value " +
                            "collection.");
                }

                Function function = new LoopFunction(_pattern, _matchResult,
                        current, startValue, boundVariable, collection);
                FunctionType functionType = new FunctionType(
                        new Type[] { BaseType.OBJECT }, BaseType.GENERAL);
                _evaluatedChildToken =
                    new FunctionToken(function, functionType);
            } else if (functionName == null) {
                Token token = null;
                try {
                    token = _evaluateChild(node, 0);
                } catch (IllegalActionException e) {
                    super.visitFunctionApplicationNode(node);
                    return;
                }

                if (token instanceof FunctionToken) {
                    FunctionToken functionToken = (FunctionToken) token;
                    int argCount = node.jjtGetNumChildren() - 1;
                    if (functionToken.getNumberOfArguments() != argCount) {
                        throw new IllegalActionException("Wrong number of "
                                + "arguments when applying function "
                                + token.toString());
                    }

                    if (functionToken.getFunction() instanceof LoopFunction) {
                        _evaluatedChildToken = functionToken.apply(new Token[] {
                                new ObjectToken(node.jjtGetChild(1)) });
                        return;
                    }
                }
                super.visitFunctionApplicationNode(node);
            }
        }

        public void visitFunctionalIfNode(ASTPtFunctionalIfNode node)
                throws IllegalActionException {
            if (node.isConstant() && node.isEvaluated()) {
                _evaluatedChildToken = node.getToken();
                return;
            }

            int numChildren = node.jjtGetNumChildren();

            if (numChildren != 3) {
                // A functional-if node MUST have three children in the parse
                // tree.
                throw new InternalErrorException(
                        "PtParser error: a functional-if node does not have "
                                + "three children in the parse tree.");
            }

            // evaluate the first sub-expression
            _evaluateChild(node, 0);

            ptolemy.data.Token test = _evaluatedChildToken;

            if (!(test instanceof BooleanToken)) {
                throw new IllegalActionException(
                        "Functional-if must branch on a boolean, but instead was "
                                + test.toString() + " an instance of "
                                + test.getClass().getName());
            }

            boolean value = ((BooleanToken) test).booleanValue();

            ASTPtRootNode tokenChild;

            if (value) {
                tokenChild = (ASTPtRootNode) node.jjtGetChild(1);
            } else {
                tokenChild = (ASTPtRootNode) node.jjtGetChild(2);
            }

            tokenChild.visit(this);

            if (node.isConstant()) {
                node.setToken(_evaluatedChildToken);
            }
        }

        public void visitMethodCallNode(ASTPtMethodCallNode node)
                throws IllegalActionException {
            int argCount = node.jjtGetNumChildren();
            if (argCount == 1) {
                Token firstChild = _evaluateChild(node, 0);
                if (firstChild instanceof NamedObjToken) {
                    NamedObjToken objectToken = (NamedObjToken) firstChild;
                    NamedObj object = objectToken.getObject();
                    NamedObj patternObject = (NamedObj) _matchResult
                            .getKey(object);
                    String methodName = node.getMethodName();
                    if (patternObject != null) {
                        NamedObj patternChild = GTTools.getChild(patternObject,
                                methodName, true, true, true, true);
                        if (patternChild != null) {
                            NamedObj child = (NamedObj) _matchResult
                                    .get(patternChild);
                            if (child != null) {
                                _evaluatedChildToken = NamedObjVariable
                                        .getNamedObjVariable(child, true)
                                        .getToken();
                                return;
                            }
                        }
                    }
                }
            }

            super.visitMethodCallNode(node);
        }

        protected Token _methodCall(String methodName, Type[] argTypes,
                Object[] argValues) throws IllegalActionException {

            Object object = argValues[0];
            if (!(object instanceof ObjectToken)
                    && !(object instanceof NamedObjToken)) {
                return super._methodCall(methodName, argTypes, argValues);
            }
            object = _getObject(object);
            if (object == null) {
                throw new IllegalActionException("Method " + methodName
                        + " cannot be found because the object invoked on is "
                        + "null.");
            }

            Class<?> clazz = object.getClass();
            Set<Class<?>> classes = new HashSet<Class<?>>();
            classes.add(clazz);
            while (!classes.isEmpty()) {
                Iterator<Class<?>> iterator = classes.iterator();
                clazz = iterator.next();
                iterator.remove();

                if (!Modifier.isPublic(clazz.getModifiers())) {
                    for (Class<?> interf : clazz.getInterfaces()) {
                        classes.add(interf);
                    }
                    Class<?> superclass = clazz.getSuperclass();
                    if (superclass != null) {
                        classes.add(superclass);
                    }
                } else {
                    Token result = _methodCall(clazz, object, methodName,
                            argTypes, argValues);
                    if (result != null) {
                        return result;
                    }
                }
            }

            return super._methodCall(methodName, argTypes, argValues);
        }

        private Object _getObject(Object value) throws IllegalActionException {
            if (value instanceof ObjectToken) {
                return ((ObjectToken) value).getValue();
            } else if (value instanceof NamedObjToken) {
                return ((NamedObjToken) value).getObject();
            } else if (value instanceof Token) {
                return ConversionUtilities
                        .convertTokenToJavaType((Token) value);
            } else {
                return value;
            }
        }

        private Token _methodCall(Class<?> clazz, Object object,
                String methodName, Type[] argTypes, Object[] argValues)
                throws IllegalActionException {
            Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                if (method.getName().equals(methodName)
                        && Modifier.isPublic(method.getModifiers())) {
                    Class<?>[] parameterTypes = method.getParameterTypes();
                    if (parameterTypes.length != argTypes.length - 1) {
                        continue;
                    }
                    boolean compatible = true;
                    for (int i = 0; compatible && i < parameterTypes.length; i++) {
                        if (!parameterTypes[i]
                                .isInstance(_getObject(argValues[i + 1]))) {
                            compatible = false;
                        }
                    }
                    if (compatible) {
                        Object[] args = new Object[argValues.length - 1];
                        for (int i = 1; i < argValues.length; i++) {
                            args[i - 1] = _getObject(argValues[i]);
                        }
                        try {
                            Object result = method.invoke(object, args);
                            if (result instanceof NamedObj) {
                                return NamedObjVariable.getNamedObjVariable(
                                        (NamedObj) result, true).getToken();
                            } else {
                                return ConversionUtilities
                                        .convertJavaTypeToToken(result);
                            }
                        } catch (IllegalArgumentException e) {
                        } catch (IllegalAccessException e) {
                        } catch (InvocationTargetException e) {
                        }
                    }
                }
            }
            return null;
        }

        private MatchResult _matchResult;

        private Pattern _pattern;
    }

    public static class LoopEvaluator extends Evaluator {

        public LoopEvaluator(Pattern pattern, MatchResult matchResult,
                String currentName, String boundVariable) {
            super(pattern, matchResult);
            _currentName = currentName;
            _boundVariable = boundVariable;
            _currentToken = Token.NIL;
            _elementToken = Token.NIL;
        }

        public void setCurrentValue(Token current) {
            _currentToken = current;
        }

        public void setElementToken(Token elementToken) {
            _elementToken = elementToken;
        }

        public void visitLeafNode(ASTPtLeafNode node)
        throws IllegalActionException {
            if (node.isIdentifier()) {
                String name = node.getName();
                if (name.equals(_currentName)) {
                    _evaluatedChildToken = _currentToken;
                    return;
                } else if (name.equals(_boundVariable)) {
                    _evaluatedChildToken = _elementToken;
                    return;
                }
            }
            super.visitLeafNode(node);
        }

        private String _boundVariable;

        private String _currentName;

        private Token _currentToken;

        private Token _elementToken;
    }

    public static class LoopFunction implements Function {

        public LoopFunction(Pattern pattern, MatchResult matchResult,
                String current, Token startValue, String boundVariable,
                Collection<?> collection) {
            _startValue = startValue;
            _collection = collection;
            _evaluator = new LoopEvaluator(pattern, matchResult, current,
                    boundVariable);
        }

        public Token apply(Token[] arguments) throws IllegalActionException {
            ASTPtRootNode tree =
                (ASTPtRootNode) ((ObjectToken) arguments[0]).getValue();
            Token currentValue = _startValue;
            for (Object element : _collection) {
                Token elementToken;
                if (element instanceof Token) {
                    elementToken = (Token) element;
                } else {
                    elementToken = new ObjectToken(element);
                }
                _evaluator.setCurrentValue(currentValue);
                _evaluator.setElementToken(elementToken);
                currentValue = _evaluator.evaluateParseTree(tree);
            }
            return currentValue;
        }

        public int getNumberOfArguments() {
            return 1;
        }

        public boolean isCongruent(Function function) {
            return false;
        }

        private Collection<?> _collection;

        private LoopEvaluator _evaluator;

        private Token _startValue;
    }

    public static class Scope implements ParserScope {

        public Scope(Pattern pattern, MatchResult matchResult,
                ParserScope superscope) {
            _pattern = pattern;
            _matchResult = matchResult;
            _superscope = superscope;
        }

        public Token get(String name) throws IllegalActionException {
            NamedObj patternChild = GTTools.getChild(_pattern, name, true,
                    true, true, true);
            if (patternChild != null &&
                    _matchResult.containsKey(patternChild)) {
                NamedObj child = (NamedObj) _matchResult.get(patternChild);
                return NamedObjVariable.getNamedObjVariable(child, true)
                        .getToken();
            } else {
                Token superToken = _superscope.get(name);
                if (superToken == null) {
                    NamedObj container = _pattern.getContainer();
                    if (container != null) {
                        NamedObjVariable containerVar =
                            NamedObjVariable.getNamedObjVariable(
                                    _pattern.getContainer(), true);
                        ParserScope containerScope =
                            containerVar.getParserScope();
                        superToken = containerScope.get(name);
                    }
                }
                return superToken;
            }
        }

        public Type getType(String name) throws IllegalActionException {
            Token token = get(name);
            if (token != null) {
                return token.getType();
            } else {
                return _superscope.getType(name);
            }
        }

        public InequalityTerm getTypeTerm(String name)
                throws IllegalActionException {
            return _superscope.getTypeTerm(name);
        }

        public Set<?> identifierSet() throws IllegalActionException {
            Set<Object> identifiers = new HashSet<Object>((Set<?>) _superscope
                    .identifierSet());
            for (Object childObject : GTTools.getChildren(_pattern, true, true,
                    true, true)) {
                identifiers.add(((NamedObj) childObject).getName());
            }
            return identifiers;
        }

        private MatchResult _matchResult;

        private Pattern _pattern;

        private ParserScope _superscope;
    }

    protected void _evaluate() throws IllegalActionException {
        //super._evaluate();
    }

    protected void _evaluate(Pattern pattern, MatchResult matchResult)
            throws IllegalActionException {
        setParseTreeEvaluator(new Evaluator(pattern, matchResult));
        super._evaluate();
    }
}
