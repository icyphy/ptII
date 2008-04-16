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
import ptolemy.kernel.util.IllegalActionException;
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
            _typeInference = new TypeInference(pattern, matchResult);
        }

        public Token evaluateParseTree(ASTPtRootNode node, ParserScope scope)
                throws IllegalActionException {
            return super.evaluateParseTree(node, new Scope(_pattern,
                    _matchResult, scope));
        }

        public void visitMethodCallNode(ASTPtMethodCallNode node)
                throws IllegalActionException {
            int argCount = node.jjtGetNumChildren();
            if (argCount == 1) {
                Token firstChild = _evaluateChild(node, 0);
                if (firstChild instanceof ObjectToken) {
                    ObjectToken objectToken = (ObjectToken) firstChild;
                    Object object = objectToken.getValue();
                    if (object instanceof NamedObj) {
                        NamedObj patternObject = (NamedObj) _matchResult.getKey(
                                object);
                        if (patternObject != null) {
                            String methodName = node.getMethodName();
                            NamedObj patternChild = GTTools.getChild(
                                    patternObject, methodName, true, true, true,
                                    true);
                            if (patternChild != null
                                    && _matchResult.containsKey(patternChild)) {
                                Object hostChild =
                                    _matchResult.get(patternChild);
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

        private MatchResult _matchResult;

        private Pattern _pattern;
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
                return new ObjectToken(child, child.getClass());
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
            try {
                _pattern.workspace().getReadAccess();
                for (Object childObject : GTTools.getChildren(_pattern, true, true,
                        true, true)) {
                    identifiers.add(((NamedObj) childObject).getName());
                }
            } finally {
                _pattern.workspace().doneReading();
            }
            return identifiers;
        }

        private MatchResult _matchResult;

        private Pattern _pattern;

        private ParserScope _superscope;
    }

    public static class TypeInference extends ParseTreeTypeInference {

        public TypeInference(Pattern pattern, MatchResult matchResult) {
            _pattern = pattern;
            _matchResult = matchResult;
        }

        public Type inferTypes(ASTPtRootNode node, ParserScope scope)
        throws IllegalActionException {
            return super.inferTypes(node, new Scope(_pattern, _matchResult,
                    scope));
        }

        public void visitMethodCallNode(ASTPtMethodCallNode node)
        throws IllegalActionException {
            int argCount = node.jjtGetNumChildren();
            if (argCount == 1) {
                Type firstChild = super._inferChild(node, 0);
                if (firstChild instanceof ObjectType) {
                    ObjectType objectType = (ObjectType) firstChild;
                    Object object = objectType.getValue();
                    if (object instanceof NamedObj) {
                        NamedObj patternObject = (NamedObj) _matchResult.getKey(
                                object);
                        if (patternObject != null) {
                            String methodName = node.getMethodName();
                            NamedObj patternChild = GTTools.getChild(
                                    patternObject, methodName, true, true, true,
                                    true);
                            if (patternChild != null
                                    && _matchResult.containsKey(patternChild)) {
                                Object hostChild =
                                    _matchResult.get(patternChild);
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

        private MatchResult _matchResult;

        private Pattern _pattern;
    }

    protected void _evaluate() throws IllegalActionException {
        super._evaluate();
    }

    protected void _evaluate(Pattern pattern, MatchResult matchResult)
            throws IllegalActionException {
        setParseTreeEvaluator(new Evaluator(pattern, matchResult));
        super._evaluate();
    }
}
