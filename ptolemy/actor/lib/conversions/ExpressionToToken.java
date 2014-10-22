/* An actor that reads expressions and parses them into tokens.

 @Copyright (c) 1998-2014 The Regents of the University of California.
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

 PT_COPYRIGHT_VERSION 2
 COPYRIGHTENDKEY
 */
package ptolemy.actor.lib.conversions;

import java.util.Set;

import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.ModelScope;
import ptolemy.data.expr.ParseTreeEvaluator;
import ptolemy.data.expr.ParserScope;
import ptolemy.data.expr.PtParser;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// ExpressionToToken

/**
 This actor reads a string expression from the input port and outputs
 the token resulting from the evaluation.  The type of the output port
 is by default undeclared. To use this actor, you may need to declare
 the output type. Depending on how the output is used, if
 backward type resolution is enabled in the model
 (the default, see the parameters of the top-level model), then
 the type will resolve to the most general type acceptable to
 the downstream actors. If the downstream actors all propagate
 type constraints backwards, then it might be possible for
 the output type to be inferred. If backward type resolution is not enabled,
 then you will always need to set the type of the output
 port to the type of the expression that is expected.
 If the input string parses to something that does not match
 the specified output data type, then a run-time type check error
 will occur when this actor tries to produce its output.
 <p>
 The expression input can refer to any variable that is in scope
 for this actor.

 @author  Steve Neuendorffer
 @version $Id$
 @since Ptolemy II 2.1
 @Pt.ProposedRating Yellow (neuendor)
 @Pt.AcceptedRating Red (liuj)
 */
public class ExpressionToToken extends Converter {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ExpressionToToken(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input.setTypeEquals(BaseType.STRING);
        // With backward type resolution, the following is not appropriate.
        // output.setTypeEquals(BaseType.GENERAL);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Output a token that results from evaluating the expression given
     *  by the input string token.
     *  @exception IllegalActionException If there is no director, or
     *   if the expression read from the input cannot be parsed.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        String string = ((StringToken) input.get(0)).stringValue();
        Token result;

        try {
            if (_parser == null) {
                _parser = new PtParser();
            }

            ASTPtRootNode parseTree = _parser.generateParseTree(string);

            if (_parseTreeEvaluator == null) {
                _parseTreeEvaluator = new ParseTreeEvaluator();
            }

            if (_scope == null) {
                _scope = new ExpressionScope();
            }

            result = _parseTreeEvaluator.evaluateParseTree(parseTree, _scope);
        } catch (IllegalActionException ex) {
            // Chain exceptions to get the actor that threw the exception.
            throw new IllegalActionException(this, ex, "Expression invalid.");
        }

        if (result == null) {
            throw new IllegalActionException(this,
                    "Expression yields a null result: " + string);
        }

        output.broadcast(result);
    }

    /** Return true if and only if an input is present.
     *  @exception IllegalActionException If there is no director, or
     *   if no connection has been made to the input.
     */
    @Override
    public boolean prefire() throws IllegalActionException {
        if (input.hasToken(0)) {
            return super.prefire();
        } else {
            return false;
        }
    }

    /** Wrapup execution of this actor.  This method overrides the
     *  base class to discard the internal parser to save memory.
     */
    @Override
    public void wrapup() {
        _parser = null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////
    private class ExpressionScope extends ModelScope {
        /** Look up and return the attribute with the specified name in the
         *  scope. Return null if such an attribute does not exist.
         *  @return The attribute with the specified name in the scope.
         */
        @Override
        public Token get(String name) throws IllegalActionException {
            Variable result = getScopedVariable(null, ExpressionToToken.this,
                    name);

            if (result != null) {
                return result.getToken();
            }

            return null;
        }

        /** Look up and return the type of the attribute with the
         *  specified name in the scope. Return null if such an
         *  attribute does not exist.
         *  @return The attribute with the specified name in the scope.
         */
        @Override
        public Type getType(String name) throws IllegalActionException {
            Variable result = getScopedVariable(null, ExpressionToToken.this,
                    name);

            if (result != null) {
                return (Type) result.getTypeTerm().getValue();
            }

            return null;
        }

        /** Look up and return the type term for the specified name
         *  in the scope. Return null if the name is not defined in this
         *  scope, or is a constant type.
         *  @return The InequalityTerm associated with the given name in
         *  the scope.
         *  @exception IllegalActionException If a value in the scope
         *  exists with the given name, but cannot be evaluated.
         */
        @Override
        public ptolemy.graph.InequalityTerm getTypeTerm(String name)
                throws IllegalActionException {
            Variable result = getScopedVariable(null, ExpressionToToken.this,
                    name);

            if (result != null) {
                return result.getTypeTerm();
            }

            return null;
        }

        /** Return the list of identifiers within the scope.
         *  @return The list of identifiers within the scope.
         */
        @Override
        public Set identifierSet() {
            return getAllScopedVariableNames(null, ExpressionToToken.this);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    /** The parser to use. */
    private PtParser _parser = null;

    /** The parse tree evaluator to use. */
    private ParseTreeEvaluator _parseTreeEvaluator = null;

    /** The scope for the parser. */
    private ParserScope _scope = null;
}
