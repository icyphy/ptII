/* ASTPtFunctionNode represents function nodes in the parse tree

 Copyright (c) 1998 The Regents of the University of California.
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
@AcceptedRating Yellow (yuhong@eecs.berkeley.edu)

Created : May 1998

*/

package ptolemy.data.expr;

import ptolemy.data.*;
import ptolemy.kernel.util.*;
import java.lang.reflect.*;
import java.util.Enumeration;
import collections.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// ASTPtFunctionNode
/**
The parse tree created from the expression string consists of a
hierarchy of node objects. This class represents function nodes in
the parse tree.
<p>
A function node is created when a function call is parsed. This node
will search for the function, using reflection, in the classes
registered for this purpose with the parser. Thus to add to the lsit
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
FIXME: need to add in ComplexToken when it is written.
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
    protected String funcName;
       

    protected ptolemy.data.Token _resolveNode()
            throws IllegalArgumentException {
        int args = jjtGetNumChildren();
        if (funcName.compareTo("eval") == 0) {
            // Have a recursive call to the parser.
            String exp = "";
            try {
                if (parser == null) {
                    System.out.println("HELP!!!");
                }
                NamedList scope = parser.getScope();
                exp = childTokens[0].stringValue();
                ASTPtRootNode tree = parser.generateParseTree(exp, scope);
                return tree.evaluateParseTree();
            } catch (IllegalActionException ex) {
                throw new IllegalArgumentException("ASTPtFunctionNode: could " +
                "not parse and evaluate expression " + exp + ", " + 
                ex.getMessage());
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("ASTPtFunctionNode: could " +
                "not parse and evaluate expression " + exp + ", " + 
                ex.getMessage());
            }
        }

        // Do not have a recursive invocation of the parser.
        Class[] argTypes = new Class[args];
        Object[] argValues = new Object[args];
        // Note: Java makes a distinction between the class objects
        // for double & Double...
        for (int i = 0; i<args; i++) {
            ptolemy.data.Token child = childTokens[i];
            if (child instanceof DoubleToken) {
                argValues[i] = new Double(((DoubleToken)child).getValue());
                argTypes[i] = Double.TYPE;
            } else if (child instanceof IntToken) {
                argValues[i] = new Integer(((IntToken)child).getValue());
                argTypes[i] = Integer.TYPE;
            } else if (child instanceof LongToken) {
                argValues[i] = new Long(((LongToken)child).getValue());
                argTypes[i] = Long.TYPE;
            } else if (child instanceof StringToken) {
                argValues[i] = new String(((StringToken)child).getValue());
                argTypes[i] = argValues[i].getClass();
            } else if (child instanceof BooleanToken) {
                argValues[i] = new Boolean(((BooleanToken)child).getValue());
                argTypes[i] = Boolean.TYPE;
            } else {
                throw new IllegalArgumentException("FunctionNode: "+
                        "Invalid argument  type, valid types are: " +
                        "boolean, complex, double, int, long  and String");
            }
        }
        // Now have the arguments converted, look through all the
        // classes registered with the parser for the appropriate function.
        Enumeration allClasses = PtParser.getRegisteredClasses().elements();
        boolean foundMethod = false;
        Object result = null;
        while (allClasses.hasMoreElements()) {
            Class nextClass = (Class)allClasses.nextElement();
            try {
                Method m = nextClass.getMethod(funcName, argTypes);
                result = m.invoke(nextClass, argValues);
                foundMethod = true;
            } catch (Exception  ex) {
                // FIXME: a lot of exceptions get caught here, perhaps
                // want to specify each of them seperately?
                //System.out.println("Method " + funcName + " not found in " +
                  //  nextClass.getName());
            }
            if (foundMethod) {
                //System.out.println("Method " + funcName + " found in " +
                  //    nextClass.getName());
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
                } else  {
                    throw new IllegalArgumentException("FunctionNode: "+
                        "result of function " + funcName + " not a valid type"+
                        ": boolean, complex, double, int, long  and String" +
                        ", or a Token.");
                }
            }
        }
        // If reach here it means the function was not found on the
        // search path.
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i<args; i++) {
            if (i == 0) {
                sb.append(argValues[i].toString());
            } else {
                sb.append(", " + argValues[i].toString());
            }
        }
        throw new IllegalArgumentException("Function " + funcName + "(" + sb +
                ") cannot be executed with given arguments.");
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
