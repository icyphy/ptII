/* ASTPtFunctionNode represents function nodes or array references in the parse tree

 Copyright (c) 1998-2001 The Regents of the University of California.
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
import ptolemy.math.FixPoint;

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
The arguments to a function and its return types can be either
Java primitive types (double, boolean, etc.), String types,
or Token types. In the case of Token types, polymorphism is supported.
That is, one can define a function foo(IntToken) and
a different function foo(DoubleToken), and the correct function
will be invoked.
<p>
@author Neil Smyth and Edward A. Lee
@version $Id$
@see ptolemy.data.expr.ASTPtRootNode
@see ptolemy.data.expr.PtParser
@see ptolemy.data.Token
@see ptolemy.data.expr.UtilityFunctions
@see java.lang.Math
*/
public class ASTPtFunctionNode extends ASTPtRootNode {

    public ASTPtFunctionNode(int id) {
        super(id);
    }

    public ASTPtFunctionNode(PtParser p, int id) {
        super(p, id);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    public void jjtClose() {
        super.jjtClose();
        if (!_isArrayRef) {
            // We cannot assume anything about a function call.
            _isConstant = false;
        }
    }

    public static Node jjtCreate(int id) {
        return new ASTPtFunctionNode(id);
    }

    public static Node jjtCreate(PtParser p, int id) {
        return new ASTPtFunctionNode(p, id);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    protected ptolemy.data.Token _resolveNode()
            throws IllegalActionException {
	int args = jjtGetNumChildren();
	if (_isArrayRef) {
	    if (args == 2) {
		// referencing an element in an array
		if (!(_childTokens[0] instanceof ArrayToken)) {
		    throw new IllegalActionException("Cannot use array "
			    + "indexing on " + _referredVar().getFullName()
			    + ": its value is not an ArrayToken.");
		}
		if (!(_childTokens[1] instanceof IntToken)) {
		    throw new IllegalActionException("The array index to "
                            + _referredVar().getFullName()
			    + " is not an integer.");
		}
		int index = ((IntToken)_childTokens[1]).intValue();
		return ((ArrayToken)_childTokens[0]).getElement(index);
	    } else if (args == 3) {
		// referencing an element in a matrix
		int row = 0;
		int col = 0;
		if (!(_childTokens[0] instanceof MatrixToken)) {
		    throw new IllegalActionException("Cannot use matrix "
		            + "indexing on " + _referredVar().getFullName()
			    + ": its value is not an MatrixToken.");
		}
		if (!(_childTokens[1] instanceof IntToken)) {
		    throw new IllegalActionException("The row index to "
                            + _referredVar().getFullName()
			    + " is not an integer.");
		} else {
		    row = ((IntToken)_childTokens[1]).intValue();
		}
		if (!(_childTokens[2] instanceof IntToken)) {
		    throw new IllegalActionException("The column index to "
                            + _referredVar().getFullName()
			    + " is not an integer.");
		} else {
		    col = ((IntToken)_childTokens[2]).intValue();
		}
		MatrixToken tok = (MatrixToken)_childTokens[0];
		return ((MatrixToken)tok).getElementAsToken(row, col);
	    } else {
		throw new IllegalActionException("Wrong number of indices "
			+ "when referencing " + _referredVar().getFullName());
	    }
	}
	if (_funcName.compareTo("eval") == 0) {
	    // Have a recursive call to the parser.
	    String exp = "";
	    if (_parser == null) {
		throw new InvalidStateException("ASTPtFunctionNode: " +
                        " recursive call to null parser.");
	    }
	    if(args == 1 && _childTokens[0] instanceof StringToken) {
		exp = ((StringToken)_childTokens[0]).stringValue();
		NamedList scope = _parser.getScope();
		ASTPtRootNode tree = _parser.generateParseTree(exp, scope);
		return tree.evaluateParseTree();
	    } else {
		throw new IllegalActionException("The function \"eval\" is" +
                        " reserved for reinvoking the parser, and takes" +
                        " exactly one String argument.");
	    }
	}

	// Do not have a recursive invocation of the parser.
	Class[] argTypes = new Class[args];
	Class[] argTokenTypes = new Class[args];
	Object[] argValues = new Object[args];
	// Note: Java makes a distinction between the class objects
	// for double & Double...
	for (int i = 0; i < args; i++) {
	    ptolemy.data.Token child = _childTokens[i];
            argTokenTypes[i] = child.getClass();
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
		argValues[i] = new String(((StringToken)child).stringValue());
		argTypes[i] = argValues[i].getClass();
	    } else if (child instanceof BooleanToken) {
		argValues[i] =
                    new Boolean(((BooleanToken)child).booleanValue());
		argTypes[i] = Boolean.TYPE;
	    } else if (child instanceof ComplexToken) {
		argValues[i] = ((ComplexToken)child).complexValue();
		argTypes[i] = argValues[i].getClass();;
	    } else if (child instanceof FixToken) {
		argValues[i] = ((FixToken)child).fixValue();
		argTypes[i] = argValues[i].getClass();;
	    } else {
		argValues[i] = child;
		argTypes[i] = argValues[i].getClass();;
	    }
	}
	// Now have the arguments converted, look through all the
	// classes registered with the parser for the appropriate function.
	Iterator allClasses = PtParser.getRegisteredClasses().iterator();
	boolean foundMethod = false;
	Object result = null;
	while (allClasses.hasNext()) {
	    Class nextClass = (Class)allClasses.next();
	    //System.out.println("ASTPtFunctionNode: " + nextClass);
	    // First we look for the method, and if we get an exception,
	    // we ignore it and keep looking.
	    try {
		Method method = nextClass.getMethod(_funcName, argTypes);
		result = method.invoke(nextClass, argValues);
		foundMethod = true;
	    } catch (NoSuchMethodException ex) {
		// We haven't found the correct function.
                // Try matching on the token classes.
                try {
                    Method method = _polymorphicGetMethod(
                            nextClass, _funcName, argTokenTypes);
                    if (method != null) {
                        result = method.invoke(nextClass, _childTokens);
                        foundMethod = true;
                    }
		} catch (SecurityException security) {
		    // If we are running under an Applet, then we
		    // may end up here if, for example, we try
		    // to invoke the non-existent quantize function on
		    // java.lang.Math.
                } catch (InvocationTargetException exception) {
                    // get the exception produced by the invoked function
                    exception.getTargetException().printStackTrace();
                    throw new IllegalActionException(
                        "Error invoking function " + _funcName + "\n" +
                        exception.getTargetException().getMessage());
		} catch (Exception exception)  {
		     throw new IllegalActionException(null, exception,
			      "Error invoking function " + _funcName
						      + " on " + nextClass);
		}

	    } catch (InvocationTargetException ex) {
		// get the exception produced by the invoked function
                ex.getTargetException().printStackTrace();
		throw new IllegalActionException(
                        "Error invoking function " + _funcName + "\n" +
                        ex.getTargetException().getMessage());
	    } catch (Exception ex)  {
		throw new IllegalActionException(ex.getMessage());
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
		} else if (result instanceof String) {
		    return new StringToken((String)result);
		} else if (result instanceof Boolean) {
		    return new BooleanToken(((Boolean)result).booleanValue());
		} else if (result instanceof Complex) {
		    return new ComplexToken((Complex)result);
		} else if (result instanceof FixPoint) {
		    return new FixToken((FixPoint)result);
		} else  {
		    throw new IllegalActionException("FunctionNode: "+
			    "result of function " + _funcName +
			    " not a valid type: boolean, complex, fixpoint" +
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
	throw new IllegalActionException("No matching function " + _funcName
                + "( " + sb + " ).");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    protected String _funcName;
    protected boolean _isArrayRef;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Return the variable being referenced when this node represents array
    // or matrix reference.
    private Variable _referredVar() {
	return ((ASTPtLeafNode)jjtGetChild(0))._var;
    }

    // Regrettably, the getMethod() method in the java Class does not
    // support polymorphism.  In particular, it does not recognize a method
    // if the class you supply for an argument type is actually derived
    // from the class in the method signature.  So we have to reimplement
    // this here.  This method returns the first method that it finds that
    // has the specified name and can be invoked with the specified
    // argument types.  It is arguable that it should return the most
    // specific method that it finds, but it turns out that this is difficult
    // to define.  So it simply returns the first match.
    // It returns null if there is no match.
    private Method _polymorphicGetMethod(
            Class library, String methodName, Class[] argTypes) {
        // NOTE: The Java docs do not explain the difference between
        // getMethods() and getDeclaredMethods(), so I'm guessing here...
        Method[] methods = library.getDeclaredMethods();
        for (int i = 0; i < methods.length; i++) {
            if (methods[i].getName().equals(methodName)) {
                Class[] arguments = methods[i].getParameterTypes();
                if (arguments.length != argTypes.length) continue;
                boolean match = true;
                for (int j = 0; j < arguments.length; j++) {
                    match = match && arguments[j].isAssignableFrom(argTypes[j]);
                }
                if (match) {
                    return methods[i];
                }
            }
        }
        return null;
    }
}

