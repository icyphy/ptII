/* ASTPtFunctionNode represents function nodes or array references in the parse tree

 Copyright (c) 1998-2000 The Regents of the University of California.
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

@ProposedRating Yellow (nsmyth@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)

Created : May 1998

*/

package ptolemy.data.expr;

import ptolemy.data.*;
import ptolemy.kernel.util.*;
import ptolemy.math.Complex;

import java.lang.reflect.*;
import java.lang.Math;		/* Needed for javadoc */
import java.util.Iterator;

//////////////////////////////////////////////////////////////////////////
//// ASTPtFunctionNode
/**
The parse tree created from the expression string consists of a
hierarchy of node objects. This class represents function nodes in
the parse tree.
<p>
A function node is created when a function call is parsed. This node
will search for the function, using reflection, in the classes
registered for this purpose with the parser. Thus to add to the list
of functions available to the expression, it is only necessary to
create a new class with the functions defined in it and register
it with the parser. By default only java.lang.Math and
ptolemy.data.expr.UtilityFunctions are searched for a given function.
<p>
The one exception to the above rule is a recursive call to the parser.
The function eval() takes as an argument a StringToken, and parses
and evaluates the contained String by re-invoking the parser. The
scope for the re-evaluation (i.e. the Parameters it can refer to
by name) is the same as the main expression in which this function
call is embedded. Note that the parse tree as it is returned from
the parser will contain a node representing this function. Then
when the tree is evaluated, the call to eval() with both create
and evaluate the parse tree for the expression argument to obtain
the Token to be stored in this node.
<p>
FIXME: add note about function argument types and the return type.
<p>
@author Neil Smyth
@version $Id$
@see ptolemy.data.expr.ASTPtRootNode
@see ptolemy.data.expr.PtParser
@see ptolemy.data.Token
@see ptolemy.data.expr.UtilityFunctions
@see java.lang.Math
*/
public class ASTPtFunctionNode extends ASTPtRootNode {
    protected String _funcName;
    protected boolean _isArrayRef;

    public void jjtClose() {
        super.jjtClose();
        if (!_isArrayRef) {
            // We cannot assume anything about a function call.
            _isConstant = false;
        }
    }

    protected ptolemy.data.Token _resolveNode()
            throws IllegalActionException {
        int args = jjtGetNumChildren();
        if (_isArrayRef) {
            int row = 0;
            int col = 0;
            if (args == 3) {
                // referencing a matrix
                if (!(_childTokens[1] instanceof IntToken)) {
                    throw new IllegalActionException("The row index to "
                            + _funcName + " is not an integer.");
                } else {
                    row = ((IntToken)_childTokens[1]).intValue();
                }
            }
            ptolemy.data.Token colTok =
                    (args == 2) ? _childTokens[1] : _childTokens[2];
            if (!(colTok instanceof IntToken)) {
                throw new IllegalActionException("The column index to "
                        + _funcName + " is not an integer.");
            } else {
                col = ((IntToken)colTok).intValue();
            }
            ptolemy.data.Token tok = _childTokens[0];
            if (tok instanceof BooleanMatrixToken) {
                boolean val = ((BooleanMatrixToken)tok).getElementAt(row, col);
                return new BooleanToken(val);
            } else if (tok instanceof IntMatrixToken) {
                int val = ((IntMatrixToken)tok).getElementAt(row, col);
                return new IntToken(val);
            } else if (tok instanceof LongToken) {
                long val = ((LongMatrixToken)tok).getElementAt(row, col);
                return new LongToken(val);
            } else if (tok instanceof DoubleMatrixToken) {
                double val = ((DoubleMatrixToken)tok).getElementAt(row, col);
                return new DoubleToken(val);
            } else if (tok instanceof ComplexMatrixToken) {
                Complex val = ((ComplexMatrixToken)tok).getElementAt(row, col);
                return new ComplexToken(val);
            } else {
                throw new IllegalActionException("The value of " + _funcName
                        + " is not a supported matrix token.");
            }
        }
        if (_funcName.compareTo("eval") == 0) {
            // Have a recursive call to the parser.
            String exp = "";
            if (_parser == null) {
                throw new InvalidStateException("ASTPtFunctionNode: " +
                        " recursive call to null parser.");
            }
            NamedList scope = _parser.getScope();
            exp = _childTokens[0].toString();
            ASTPtRootNode tree = _parser.generateParseTree(exp, scope);
            return tree.evaluateParseTree();
        }

        // Do not have a recursive invocation of the parser.
        Class[] argTypes = new Class[args];
        Object[] argValues = new Object[args];
        // Note: Java makes a distinction between the class objects
        // for double & Double...
        for (int i = 0; i < args; i++) {
            ptolemy.data.Token child = _childTokens[i];
            if (child instanceof DoubleToken) {
                argValues[i] = new Double(((DoubleToken)child).doubleValue());
                argTypes[i] = Double.TYPE;
            } else if (child instanceof IntToken) {
                argValues[i] = new Integer(((IntToken)child).intValue());
                argTypes[i] = Integer.TYPE;
            } else if (child instanceof LongToken) {
                argValues[i] = new Long(((LongToken)child).longValue());
                argTypes[i] = Long.TYPE;
            } else if (child instanceof StringToken) {
                argValues[i] = new String(((StringToken)child).toString());
                argTypes[i] = argValues[i].getClass();
            } else if (child instanceof BooleanToken) {
                argValues[i] =
                    new Boolean(((BooleanToken)child).booleanValue());
                argTypes[i] = Boolean.TYPE;
            } else if (child instanceof ComplexToken) {
                argValues[i] = ((ComplexToken)child).complexValue();
                argTypes[i] = argValues[i].getClass();;
            } else if (child instanceof IntMatrixToken) {
                argValues[i] = child;
                argTypes[i] = argValues[i].getClass();;
            } else if (child instanceof DoubleMatrixToken) {
                argValues[i] = child;
                argTypes[i] = argValues[i].getClass();;
            } else if (child instanceof ComplexMatrixToken) {
                argValues[i] = child;
                argTypes[i] = argValues[i].getClass();;
            } else {
                throw new IllegalActionException("FunctionNode: "+
                        "Invalid argument  type, valid types are: " +
                        "boolean, complex, double, int, long, String" +
                        "IntMatrixToken, DoubleMatrixToken, " +
                        "ComplexMatrixToken. " );
            }
            // FIXME: what is the TYPE that needs to be filled
            // in in the argValues[]. Current it is from the
            // child.
        }
        // Now have the arguments converted, look through all the
        // classes registered with the parser for the appropriate function.
        Iterator allClasses = PtParser.getRegisteredClasses().iterator();
        boolean foundMethod = false;
        Object result = null;
        while (allClasses.hasNext()) {
            Class nextClass = (Class)allClasses.next();
            System.out.println("ASTPtFunctionNode: " + nextClass);
            // First we look for the method, and if we get an exception,
            // we ignore it and keep looking.
            try {
                Method method = nextClass.getMethod(_funcName, argTypes);
                // Then we invoke it, and report errors.
                try {
                    // System.out.println("ASTPtFunctionNode: Method:" +
                    //        method + 
                    //        " nextClass: " + nextClass +
                    //        " argValues: " + argValues);
                    result = method.invoke(nextClass, argValues);
                    foundMethod = true;
                } catch (Exception  ex) {
                    System.out.println("Invocation of method " + _funcName +
                            " in " + nextClass.getName() + " threw: " + ex);
                    ex.printStackTrace();
                }
            } catch (Exception  ex) {
                // FIXME: a lot of exceptions get caught here, perhaps
                // want to specify each of them separately?
                //System.out.println("Method " + _funcName + " not found in " +
                //        nextClass.getName() + ": " + ex);
                //ex.printStackTrace();
            }

            if (foundMethod) {
                if (result instanceof ptolemy.data.Token) {
                    return (ptolemy.data.Token)result;
                } else if (result instanceof Double) {
                    return new DoubleToken(((Double)result).doubleValue());
                } else if (result instanceof Integer) {
                    return new IntToken(((Integer)result).intValue());
                } else if (result instanceof Long) {
                    return new LongToken(((Long)result).longValue());
                } else if (result instanceof Long) {
                    return new LongToken(((Long)result).longValue());
                } else if (result instanceof String) {
                    return new StringToken((String)result);
                } else if (result instanceof Boolean) {
                    return new BooleanToken(((Boolean)result).booleanValue());
                } else if (result instanceof Complex) {
                    return new ComplexToken((Complex)result);
                } else  {
                    throw new IllegalActionException("FunctionNode: "+
                            "result of function " + _funcName +
                            " not a valid type: boolean, complex, " +
                            " double, int, long  and String, or a Token.");
                }
            }
        }
        // If reach here it means the function was not found on the
        // search path.
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < args; i++) {
            if (i == 0) {
                sb.append(argValues[i].toString());
            } else {
                sb.append(", " + argValues[i].toString());
            }
        }
        throw new IllegalActionException("ASTFunction Function " + _funcName
                + "( " + sb + " ) cannot be executed with given arguments.");
    }

    public ASTPtFunctionNode(int id) {
        super(id);
    }

    public ASTPtFunctionNode(PtParser p, int id) {
        super(p, id);
    }

    public static Node jjtCreate(int id) {
        return new ASTPtFunctionNode(id);
    }

    public static Node jjtCreate(PtParser p, int id) {
        return new ASTPtFunctionNode(p, id);
    }
}
