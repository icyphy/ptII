/* ASTPtFunctionNode represents function nodes or array references in the parse tree

 Copyright (c) 1998-2001 The Regents of the University of California and
 Research in Motion Limited.
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

@ProposedRating Yellow (nsmyth@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)

Created : May 1998
*/

package ptolemy.data.expr;

import ptolemy.data.*;
import ptolemy.kernel.util.*;
import ptolemy.math.Complex;
import ptolemy.math.FixPoint;
import ptolemy.math.IntegerMatrixMath;
import ptolemy.math.DoubleMatrixMath;
import ptolemy.math.ComplexMatrixMath;
import ptolemy.matlab.Engine;
import java.lang.reflect.*;
import java.lang.Math;		/* Needed for javadoc */
import java.util.Iterator;
import java.util.StringTokenizer;

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
@author Zoltan Kemenczy, Research in Motion Limited
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
        boolean debug = false;
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
        if (_funcName.compareTo("matlab") == 0) {
	    if (_childTokens[0] instanceof StringToken) {
                // Invoke the matlab engine to evaluate this function
                String exp = ((StringToken)_childTokens[0]).stringValue();
                NamedList scope = _parser.getScope();
                Engine matlabEngine = new Engine();
                ptolemy.data.Token result = null;
                matlabEngine.open();
                try {
                    String addPathCommand = null;         // Assume none
                    ptolemy.data.Token previousPath = null;
                    Variable packageDirectories =
                        (Variable)scope.get("packageDirectories");
                    if (packageDirectories != null) {
                        StringTokenizer dirs = new
                            StringTokenizer((String)
                                        ((StringToken)packageDirectories
                                         .getToken()).stringValue(),",");
                        StringBuffer cellFormat = new StringBuffer(512);
                        cellFormat.append("{");
                        if (dirs.hasMoreTokens()) {
                            cellFormat.append("'" + UtilityFunctions
				      .findFile(dirs.nextToken()) + "'");
                        }
                        while (dirs.hasMoreTokens()) {
                            cellFormat.append(",'" + UtilityFunctions
				      .findFile(dirs.nextToken()) + "'");
                        }
                        cellFormat.append("}");

                        if (cellFormat.length() > 2) {
                            addPathCommand = "addedPath_=" + cellFormat.toString()
                                + ";addpath(addedPath_{:});";
                            matlabEngine.evalString("previousPath_=path");
                            previousPath = matlabEngine.get("previousPath_");
                        }
                    }
                    matlabEngine.evalString("clear variables;clear globals");

                    if (addPathCommand != null)
                        matlabEngine.evalString(addPathCommand);

                    // Set scope variables
                    Iterator variables = scope.elementList().iterator();
                    while(variables.hasNext()) {
                        Variable var = (Variable)variables.next();
                        if (var != packageDirectories)
                            matlabEngine.put(var.getName(), var.getToken());
                    }
                    matlabEngine.evalString("result__="+exp);
                    result = matlabEngine.get("result__");
                }
                finally {
                    matlabEngine.close();
                }
                return result;
            } else {
		throw new IllegalActionException("The function \"matlab\" is" +
                        " reserved for invoking the matlab engine, and takes" +
                        " a string matlab expression argument followed by" +
                        " names of input variables used in the expression.");
	    }
        }
	// Do not have a recursive invocation of the parser.

        // First try to find a signature using argument token values.
	Class[] argTypes = new Class[args];
	Object[] argValues = new Object[args];

	for (int i = 0; i < args; i++) {
            argValues[i] = (ptolemy.data.Token)_childTokens[i];
            argTypes[i] = argValues[i].getClass();
        }
        Object result = FindAndRunMethod(_funcName, argTypes, argValues);

        if (result == null) {

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
                    argValues[i] = new String(((StringToken)child).stringValue());
                    argTypes[i] = argValues[i].getClass();
                } else if (child instanceof BooleanToken) {
                    argValues[i] =
                        new Boolean(((BooleanToken)child).booleanValue());
                    argTypes[i] = Boolean.TYPE;
                } else if (child instanceof ComplexToken) {
                    argValues[i] = ((ComplexToken)child).complexValue();
                    argTypes[i] = argValues[i].getClass();
                } else if (child instanceof FixToken) {
                    argValues[i] = ((FixToken)child).fixValue();
                    argTypes[i] = argValues[i].getClass();
		} else if (child instanceof FixMatrixToken) {
		    argValues[i] = ((FixMatrixToken)child).fixMatrix();
		    argTypes[i] = argValues[i].getClass();
                } else if (child instanceof IntMatrixToken) {
                    argValues[i] = ((IntMatrixToken)child).intMatrix();
                    argTypes[i] = argValues[i].getClass();
                } else if (child instanceof DoubleMatrixToken) {
                    argValues[i] = ((DoubleMatrixToken)child).doubleMatrix();
                    argTypes[i] = argValues[i].getClass();
                } else if (child instanceof ComplexMatrixToken) {
                    argValues[i] = ((ComplexMatrixToken)child).complexMatrix();
                    argTypes[i] = argValues[i].getClass();
                } else if (child instanceof LongMatrixToken) {
                    argValues[i] = ((LongMatrixToken)child).longMatrix();
                    argTypes[i] = argValues[i].getClass();
                } else if (child instanceof ArrayToken) {
                    // This is frustrating... It would be nice if there was a
                    // Token.getValue() that would return the token element value in
                    // a polymorphic way...
                    if (((ArrayToken)child).getElement(0) instanceof FixToken) {
                        // special case of Array of GSMFixTokens
                        FixPoint[] array = new FixPoint[((ArrayToken)child).length()];
                        for (int j = 0; j < array.length; j++) {
                            array[j] = ((FixToken)((ArrayToken)child).getElement(j)).fixValue();
                        }
                        argValues[i] = array;
                    } else if (((ArrayToken)child).getElement(0) instanceof IntToken) {
                        int[] array = new int[((ArrayToken)child).length()];
                        for (int j = 0; j < array.length; j++) {
                            array[j] = ((IntToken)((ArrayToken)child).getElement(j)).intValue();
                        }
                        argValues[i] = array;
                    } else if (((ArrayToken)child).getElement(0) instanceof DoubleToken) {
                        double[] array = new double[((ArrayToken)child).length()];
                        for (int j = 0; j < array.length; j++) {
                            array[j] = ((DoubleToken)((ArrayToken)child).getElement(j)).doubleValue();
                        }
                        argValues[i] = array;
                    } else if (((ArrayToken)child).getElement(0) instanceof ComplexToken) {
                        Complex[] array = new Complex[((ArrayToken)child).length()];
                        for (int j = 0; j < array.length; j++) {
                            array[j] = ((ComplexToken)((ArrayToken)child).getElement(j)).complexValue();
                        }
                        argValues[i] = array;
                    }
                    argTypes[i] = argValues[i].getClass();
                } else {
                    argValues[i] = child;
                    argTypes[i] = argValues[i].getClass();
                }
                if (debug) System.out.println("Arg "+i+": "+child);

                // FIXME: what is the TYPE that needs to be filled
                // in the argValues[]. Current it is from the
                // child.
            }
            // Now have the arguments converted, look through all the
            // classes registered with the parser for the appropriate function.
            result = FindAndRunMethod(_funcName, argTypes, argValues);
        }
        if (debug) System.out.println("function: "+_funcName);

        if (result != null) {
            ptolemy.data.Token retval = null;
            if (result instanceof ptolemy.data.Token) {
                retval = (ptolemy.data.Token)result;
            } else if (result instanceof Double) {
                retval = new DoubleToken(((Double)result).doubleValue());
            } else if (result instanceof Integer) {
                retval = new IntToken(((Integer)result).intValue());
            } else if (result instanceof Long) {
                retval = new LongToken(((Long)result).longValue());
            } else if (result instanceof String) {
                retval = new StringToken((String)result);
            } else if (result instanceof Boolean) {
                retval = new BooleanToken(((Boolean)result).booleanValue());
            } else if (result instanceof Complex) {
                retval = new ComplexToken((Complex)result);
            } else if (result instanceof FixPoint) {
                retval = new FixToken((FixPoint)result);
	    } else if (result instanceof int[][]) {
		retval = new IntMatrixToken((int[][])result);
	    } else if (result instanceof double[][]) {
		retval = new DoubleMatrixToken((double[][])result);
	    } else if (result instanceof Complex[][]) {
		retval = new ComplexMatrixToken((Complex[][])result);
	    } else if (result instanceof long[][]) {
		retval = new LongMatrixToken((long[][])result);
	    } else if (result instanceof FixPoint[][]) {
		retval = new FixMatrixToken((FixPoint[][])result);
	    } else if (result instanceof double[]) {
		DoubleToken[] temp = new DoubleToken[((double[])result).length];
		for (int j = 0; j < temp.length; j++) {
		    temp[j] = new DoubleToken(((double[])result)[j]);
		}
		retval = new ArrayToken(temp);
	    } else if (result instanceof Complex[]) {
		ComplexToken[] temp = new ComplexToken[((Complex[])result).length];
		for (int j = 0; j < temp.length; j++) {
		    temp[j] = new ComplexToken(((Complex[])result)[j]);
		}
		retval = new ArrayToken(temp);
	    } else if (result instanceof int[]) {
		IntToken[] temp = new IntToken[((int[])result).length];
		for (int j = 0; j < temp.length; j++) {
		    temp[j] = new IntToken(((int[])result)[j]);
		}
		retval = new ArrayToken(temp);
	    } else if (result instanceof long[]) {
		LongToken[] temp = new LongToken[((long[])result).length];
		for (int j = 0; j < temp.length; j++) {
		    temp[j] = new LongToken(((long[])result)[j]);
		}
		retval = new ArrayToken(temp);

	    } else if (result instanceof FixPoint[]) {
		// Create back an ArrayToken containing FixTokens
		FixToken[] temp = new FixToken[((FixPoint[])result).length];
		for (int j = 0; j < temp.length; j++) {
		    temp[j] = new FixToken(((FixPoint[])result)[j]);
		}
		retval = new ArrayToken(temp);
            } else {
                throw new IllegalActionException("FunctionNode: "+
                                                 "result of function " + _funcName +
                                                 " not a valid type: boolean, complex, fixpoint" +
                                                 " double, int, long  and String, or a Token; or:" +
                                                 " int[][], double[][], Complex[][], int[], double[], Complex[]"
                                                 );
            }
            if (debug) System.out.println("result:  "+retval);
            return retval;
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

   private Object FindAndRunMethod(
	String funcName,
        Class[] argTypes,
        Object[] argValues
	) throws IllegalActionException {

	// First try to find a matching method with argTypes as is...

	Object result = null;
	Method method = null;
        Iterator allClasses = PtParser.getRegisteredClasses().iterator();
        while (allClasses.hasNext() && result == null) {
            Class nextClass = (Class)allClasses.next();
	    //System.out.println("ASTPtFunctionNode: " + nextClass);
	    // First we look for the method, and if we get an exception,
	    // we ignore it and keep looking.
	    try {
		method = nextClass.getMethod(_funcName, argTypes);
		result = method.invoke(nextClass, argValues);
	    } catch (NoSuchMethodException ex) {
		// We haven't found the correct function.
                // Try matching on argument type sub-classes.
                try {
                    method = _polymorphicGetMethod
                        (nextClass, _funcName, argTypes);
                    if (method != null) {
                        result = method.invoke(nextClass, argValues);
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
        }

	// If that failed, then try to reduce argument dimensions if possible
	// and try again (recursively)

	if (result == null) {
	    // Check if any arguments are of array type and, if any are, that they
	    // all have the same length.
	    boolean resIsArray = false;
	    int dim = 0;
	    Class[] nArgTypes = new Class[argTypes.length];
	    Object[] nArgValues = new Object[argValues.length];
	    for (int i = 0; i < argTypes.length; i++) {
		resIsArray |= argTypes[i].isArray();
		if (argTypes[i].isArray()) {
		    if (dim != 0 && Array.getLength(argValues[i]) != dim) {
                        // This argument does not have the same dimension as the
                        // first array argument encountered. Cannot recurse
                        // using this approach...
			resIsArray = false;
			break;
		    }
		    else {
                        // First array argument encounter
			dim = Array.getLength(argValues[i]);
			nArgTypes[i] = argTypes[i].getComponentType();
		    }
		}
		else {
		    nArgTypes[i] = argTypes[i];
		}
	    }
	    // If we found consistent array parameters, their dimensions have
	    // been reduced. Try method matching again
	    for (int d = 0; resIsArray && d < dim; d++) {
		for (int i = 0; i < argValues.length; i++) {
		    if (argTypes[i].isArray()) {
			nArgValues[i] = Array.get(argValues[i],d);
		    }
		    else {
			nArgValues[i] = argValues[i];
		    }
		}
		Object a = FindAndRunMethod(funcName, nArgTypes, nArgValues);
		if (a == null)
		    break;
		Class c = a.getClass();
		if (a instanceof Double) c = Double.TYPE;
		if (a instanceof Integer) c = Integer.TYPE;
		if (a instanceof Long) c = Long.TYPE;
                if (result == null) {
                    result = Array.newInstance(c, dim);
                    Array.set(result,0,a);
                }
                else
                    Array.set(result,d,a);
	    }
	}
	return result;
    }

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

