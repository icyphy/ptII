/* A visitor for parse trees of the expression language.

 Copyright (c) 1998-2003 The Regents of the University of California
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA OR RESEARCH IN MOTION
 LIMITED BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT, SPECIAL,
 INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT OF THE USE OF THIS
 SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE UNIVERSITY OF CALIFORNIA
 OR RESEARCH IN MOTION LIMITED HAVE BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA AND RESEARCH IN MOTION LIMITED
 SPECIFICALLY DISCLAIM ANY WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 PARTICULAR PURPOSE. THE SOFTWARE PROVIDED HEREUNDER IS ON AN "AS IS"
 BASIS, AND THE UNIVERSITY OF CALIFORNIA AND RESEARCH IN MOTION
 LIMITED HAVE NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

@ProposedRating Yellow (neuendor@eecs.berkeley.edu)
@AcceptedRating Red (neuendor@eecs.berkeley.edu)

*/
package ptolemy.data.expr;

import ptolemy.data.FunctionToken;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.kernel.util.IllegalActionException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//////////////////////////////////////////////////////////////////////////
//// ExpressionFunction
/**
An implementation of a function closure that encapsulates an
expression tree.  Instances of this class are created during the
evaluation of function closure expressions in the expression language,
like "function(x:int, y:int) x+y".

@author Steve Neuendorffer, Xiaojun Liu
@version $Id$
@since Ptolemy II 2.1
@see ptolemy.data.expr.ASTPtRootNode
*/

public class ExpressionFunction implements FunctionToken.Function {
    public ExpressionFunction(List argumentNames, Type[] argumentTypes,
            ASTPtRootNode exprRoot) {
        _argumentNames = new ArrayList(argumentNames);
        _argumentTypes = argumentTypes;
        _exprRoot = exprRoot;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Apply the function to the list of arguments, which are tokens.
     *  @param arguments The list of arguments.
     *  @return The result of applying the function to the given
     *   arguments.
     *  @exception IllegalActionException If thrown during evaluating
     *   the function.
     */
    public ptolemy.data.Token apply(ptolemy.data.Token[] arguments)
            throws IllegalActionException {
        ParseTreeEvaluator parseTreeEvaluator = new ParseTreeEvaluator();
        // construct a NamedConstantsScope that contains mappings from
        // argument names to the given argument values
        Map map = new HashMap();
        for (int i = 0; i < arguments.length; ++i) {
            String name = (String)_argumentNames.get(i);
            ptolemy.data.Token argument = arguments[i];
            map.put(name, argument);
        }
        NamedConstantsScope argumentsScope = new NamedConstantsScope(map);
        return parseTreeEvaluator.evaluateParseTree(
                _exprRoot, argumentsScope);
    }

    /** Return the number of arguments of the function.
     *  @return The number of arguments of the function.
     */
    public int getNumberOfArguments() {
        return _argumentNames.size();
    }

    /** Return true if this function is congruent to the given
     *  function.  Classes should implement this method so that
     *  two functions are congruent under any renaming of the
     *  bound variables of the function.  For simplicity, a
     *  function need only be congruent to other functions of the
     *  same class.
     *  @param function The function to check congruency against.
     */
    public boolean isCongruent(FunctionToken.Function function) {
        return toString().compareTo(function.toString()) == 0;

        // FIXME: The above is not terribly nice...  It would be nice
        // to allow function equivalence under bound variable
        // renaming.  However, I got stuck trying to implement this,
        // and decided I didn't want to spend any more time on it...
        // SN - 4/18/2003

        /**
           if (!(function instanceof ExpressionFunction)) {
           return false;
           }
           ExpressionFunction expressionFunction = (ExpressionFunction)function;
           // The functions must have the same number of arguments.
           if (getNumberOfArguments() != function.getNumberOfArguments()) {
           return false;
           }
           // Construct the renaming map.
           Map renaming = new HashMap();
           Iterator argNames = expressionFunction._argumentNames.iterator();
           for (Iterator names = _argumentNames.iterator();
           names.hasNext();) {
           String name = (String)names.next();
           String argName = (String)argNames.next();
           renaming.put(name, argName);
           }
           return _exprRoot.isCongruent(expressionFunction._exprRoot,
           renaming);
        */
    }

    /** Return a string representation of this function.
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer("(function(");
        int n = _argumentNames.size();
        for (int i = 0; i < n; i++) {
            if (i > 0) {
                buffer.append(", ");
            }
            buffer.append((String)_argumentNames.get(i));
            Type type = _argumentTypes[i];
            if (type != BaseType.GENERAL) {
                buffer.append(":");
                buffer.append(type.toString());
            }
        }
        buffer.append(") ");
        ParseTreeWriter writer = new ParseTreeWriter();
        String string = writer.printParseTree(_exprRoot);
        buffer.append(string);
        buffer.append(")");
        return buffer.toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                       private variables                   ////

    // The root of the expression tree.
    private ASTPtRootNode _exprRoot;
    // The list of argument names that are bound in this function closure.
    private List _argumentNames;
    // The list of the (monomorphic) types of the arguments.
    private Type[] _argumentTypes;
}
