/* An actor that reads expressions and parses them into tokens.

@Copyright (c) 1998-2003 The Regents of the University of California.
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
@ProposedRating Yellow (neuendor@eecs.berkeley.edu)
@AcceptedRating Red (liuj@eecs.berkeley.edu)
*/

package ptolemy.actor.lib.conversions;

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

import java.util.Set;

//////////////////////////////////////////////////////////////////////////
//// ExpressionToToken
/**
This actor reads a string expression from the input port and outputs
the token resulting from the evaluation.  The type of the output port
defaults to general, meaning that the only output will be a pure
event.  In order to usefully use this class, the type of the output
port must be set to the type of the expression that is expected.

@author  Steve Neuendorffer
@version $Id$
@since Ptolemy II 2.1
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
        output.setTypeEquals(BaseType.GENERAL);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Output a token that results from evaluating the expression given
     *  by the input string token.
     *  @exception IllegalActionException If there is no director, or
     *   if the expression read from the input cannot be parsed.
     */
    public void fire() throws IllegalActionException {
        String string = ((StringToken)input.get(0)).stringValue();
        Token result;
        try {
            if(_parser == null) {
                _parser = new PtParser();
            }
            ASTPtRootNode parseTree = _parser.generateParseTree(string);
            
            if (_parseTreeEvaluator == null) {
                _parseTreeEvaluator = new ParseTreeEvaluator();
            }
            if (_scope == null) {
                _scope = new ExpressionScope();
            }
            result = _parseTreeEvaluator.evaluateParseTree(
                    parseTree, _scope);
        } catch (IllegalActionException ex) {
            // Chain exceptions to get the actor that threw the exception.
            throw new IllegalActionException(this, ex, "Expression invalid.");
        }
        
        if (result == null) {
            throw new IllegalActionException(this,
                    "Expression yields a null result: " +
                    string);
        }
        output.broadcast(result);
    }

    /** Return true if and only if an input is present.
     *  @exception IllegalActionException If there is no director, or
     *   if no connection has been made to the input.
     */
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
    public void wrapup() { 
        _parser = null; 
    }

    ///////////////////////////////////////////////////////////////////
    ////                        inner classes                      ////

    private class ExpressionScope extends ModelScope {

        /** Look up and return the attribute with the specified name in the
         *  scope. Return null if such an attribute does not exist.
         *  @return The attribute with the specified name in the scope.
         */
        public Token get(String name) throws IllegalActionException {
            Variable result = getScopedVariable(null, 
                    ExpressionToToken.this, name);
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
        public Type getType(String name) throws IllegalActionException {
            Variable result = 
                getScopedVariable(null, ExpressionToToken.this, name);
            if (result != null) {
                return (Type)result.getTypeTerm().getValue();
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
        public ptolemy.graph.InequalityTerm getTypeTerm(String name)
                throws IllegalActionException {
            Variable result = 
                getScopedVariable(null, ExpressionToToken.this, name);
            if (result != null) {
                return result.getTypeTerm();
            }
            return null;
        }
        
        /** Return the list of identifiers within the scope.
         *  @return The list of identifiers within the scope.
         */
        public Set identifierSet() {
            return getAllScopedVariableNames(null, ExpressionToToken.this);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////
    
    private PtParser _parser = null;
    private ParseTreeEvaluator _parseTreeEvaluator = null;
    private ParserScope _scope = null;
}
