/* CachedMethod provides a fast hashtable based lookup for methods

 Copyright (c) 1998-2003 The Regents of the University of California and
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

@ProposedRating Yellow (neuendor@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)

Created : May 2002
*/
package ptolemy.data.expr;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.data.type.*;
import ptolemy.data.ArrayToken;
import ptolemy.data.MatrixToken;

import java.lang.reflect.Method;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.Hashtable;
import java.util.Iterator;

//////////////////////////////////////////////////////////////////////////
//// CachedMethod
/** Cache information about methods invoked from the Ptolemy expression
 parser (PtParser).  This includes both functions that are registered with
the parser, and methods that are invoked on tokens.

Searching all the registered function classes for a method
repetitively every time when a Parameter containing a function
expression is evaluated becomes very expensive.  This is especially
true when using polymorphic reflection to find methods, and automatic
type conversions to find methods that take native types.  This class
provides a cache of the function/method signatures already analyzed, so
that next time when the same method with the same signature is invoked
the search is replaced by fast hashed access to the cache.<p>

{@link #REAL} {@link #FUNCTION}s exist in classes registered with
PtParser.
{@link #REAL} {@link #METHOD}s exist in various token class instances
(Tokens or other).
{@link #MISSING} methods are the ones for which the search
has failed. The existence of either of these in the cache avoids
the expensive (function) search through all the registered
function classes, at the expense of additional memory management.

The cache is cleared by PtParser.registerFunctionClass() so that any
changes to the registered function classes cause the cache to be
re-generated.<p>

Note that this class maintains the cache totally using instances of
ptolemy.data.type.Type.  While this is somewhat constraining since it
prevents using this class for reflecting things that are not methods
on ptolemy tokens, it makes it much easier to maintain the cache,
perform coherent type conversions on invocation, and generate code from
the expression language.

@author Zoltan Kemenczy, Research in Motion Limited., Steve Neuendorffer
@version $Id$
@since Ptolemy II 2.0
@see ptolemy.data.expr.ASTPtFunctionNode
@see ptolemy.data.expr.PtParser
*/

public class CachedMethod {

    /** Construct a new CachedMethod and compute its hashcode. */
    public CachedMethod(String methodName, Type[] argTypes,
            Method method, ArgumentConversion[] conversions, int type) {
        // Note clones for safety...
        _methodName = methodName;
        _argTypes = (Type[]) argTypes.clone();
        _method = method;
        if(conversions != null) {
            _conversions = (ArgumentConversion[]) conversions.clone();
        } else {
            _conversions = null;
        }
        _type = type;

        // compute the hashcode, based on the method name and argument types.
        _hashcode = methodName.hashCode();
        for (int i = 0; i < argTypes.length; i++) {
            _hashcode += argTypes[i].hashCode();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** A "missing" method that is not real and cannot be constructed. */
    public static final int MISSING = 1;

    /** A method "constructed" by ASTPtFunctionNode by reducing argument
        array dimensions to reach a real method . */
    // public static final int CONSTRUCTED = 2;

    /** A method/function that is "real" - found in class with the
        specified signature. */
    public static final int REAL = 4;

    /** A function (could end up real/constructed or missing). */
    public static final int FUNCTION = 8;

    /** A method (could end up real/constructed or missing). */
    public static final int METHOD = 16;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return true if the argument represents the same CachedMethod as
        this. Required by Hashtable. */
    public boolean equals(Object arg) {
        boolean retval = true;
        if(!(arg instanceof CachedMethod)) {
            return false;
        }
        CachedMethod argMethod = (CachedMethod)arg;
        if (!_methodName.equals(argMethod._methodName)) {
            return false;
        }
        if ((_type & (FUNCTION+METHOD)) !=
                (argMethod._type & (FUNCTION+METHOD)))
            return false;
        if (_argTypes.length != argMethod._argTypes.length)
            return false;
        for (int i = 0; i < _argTypes.length; i++) {
            if (!_argTypes[i].equals(argMethod._argTypes[i]))
                return false;
        }
        return true;
    }

    public ArgumentConversion[] getConversions() {
        return _conversions;
    }

    /** Return the type of the token that results from an invocation
     *  of this method.  If this method is missing, then an exception
     *  will be thrown.
     */
    public Type getReturnType() throws IllegalActionException {
        if(isMissing()) {
            throw new IllegalActionException("The return type of the method "
                    + toString() + " cannot be determined because "
                    + "no matching method was found.");
        }
        Class returnType = _method.getReturnType();
        Type type = ASTPtFunctionNode.convertJavaTypeToTokenType(returnType);
        return type;
    }

    /** Return the hashcode calculated when this was constructed. */
    public int hashCode() {
        return _hashcode;
    }

    public boolean isMissing() {
        return (_type & MISSING) == MISSING;
    }
    public boolean isReal() {
        return (_type & REAL) == REAL;
    }
    public boolean isFunction() {
        return (_type & FUNCTION) == FUNCTION;
    }
    public boolean isMethod() {
        return (_type & METHOD) == METHOD;
    }

    /** Find method specified by its methodName, argument types
     * and argument values by first checking in the method cache
     * followed by checking the first argument's class, followed by
     * checking all the function classes registered with PtParser.
     */
    public static CachedMethod findMethod(String methodName,
            Type[] argTypes,
            int type
     ) throws IllegalActionException {
        //System.out.println("findMethod(" + methodName + ")");
        // Check to see if there is a cache already.
        CachedMethod cachedMethod = get(methodName, argTypes, type);
        if (cachedMethod != null) {
            //     System.out.println("inCache");
            return cachedMethod;
        }
        //  System.out.println("notInCache");

        if (type == METHOD) {
            // Try to reflect the method.
            int num = argTypes.length;
            ArgumentConversion[] conversions =
                new ArgumentConversion[num - 1];

            Class destTokenClass = argTypes[0].getTokenClass();
            Type[] methodArgTypes;
      	    if (num == 1) {
                methodArgTypes = null;
            } else {
                methodArgTypes = new Type[num - 1];
                for (int i = 1; i < num; i++) {
                    methodArgTypes[i-1] = argTypes[i];
                }
            }

            try {
                Method method = _polymorphicGetMethod(destTokenClass,
                        methodName, methodArgTypes, conversions);
                if(method != null) {
                    cachedMethod = new CachedMethod(methodName, argTypes,
                            method, conversions, type+REAL);
                }
           } catch (SecurityException security) {
                // If we are running under an Applet, then we
                // may end up here if, for example, we try
                // to invoke the non-existent quantize function on
                // java.lang.Math.
            }

            if(cachedMethod == null) {
                // Native convert the base class.
                //  System.out.println("Checking for array map");
                destTokenClass =
                    ASTPtFunctionNode.convertTokenTypeToJavaType(argTypes[0]);

                Method method = _polymorphicGetMethod(destTokenClass,
                        methodName, methodArgTypes, conversions);
                if(method != null) {
                    cachedMethod = new BaseConvertCachedMethod(
                            methodName, argTypes, method, NATIVE, conversions,
                            type+REAL);
                }
            }

        } else { //if(type == FUNCTION) {
            ArgumentConversion[] conversions =
                new ArgumentConversion[argTypes.length];
            // Search the registered function classes
            Iterator allClasses =
                PtParser.getRegisteredClasses().iterator();
            while (allClasses.hasNext() && cachedMethod == null) {
                Class nextClass = (Class)allClasses.next();
                //System.out.println("ASTPtFunctionNode: " + nextClass);
                try {
                    Method method = _polymorphicGetMethod
                        (nextClass, methodName, argTypes, conversions);
                    if(method != null) {
                       cachedMethod = new CachedMethod(methodName, argTypes,
                               method, conversions, type+REAL);
                    }
                } catch (SecurityException security) {
                    // If we are running under an Applet, then we
                    // may end up here if, for example, we try
                    // to invoke the non-existent quantize function on
                    // java.lang.Math.
                }
            }
        }

        if(cachedMethod == null) {
            // System.out.println("Checking for array map");

            // Go Look for an ArrayMapped method, instead.
 	    // Check if any arguments are of array type.
	    boolean hasArray = false;
            boolean[] isArrayArg = new boolean[argTypes.length];
	    Type[] newArgTypes = new Type[argTypes.length];
            for (int i = 0; i < argTypes.length; i++) {
                //   System.out.println("argType[" + i + "] = " + argTypes[i].getClass());
                if (argTypes[i] instanceof ArrayType) {
                    hasArray = true;
                    newArgTypes[i] = ((ArrayType)argTypes[i]).getElementType();
                    isArrayArg[i] = true;
                } else {
		    newArgTypes[i] = argTypes[i];
                    isArrayArg[i] = false;
		}
	    }

            if(hasArray) {
                CachedMethod mapCachedMethod =
                    findMethod(methodName, newArgTypes, type);
                if(!mapCachedMethod.isMissing()) {
                    cachedMethod = new ArrayMapCachedMethod(
                            methodName, argTypes, type+REAL,
                            mapCachedMethod, isArrayArg);
                }
            }
        }

        if(cachedMethod == null) {
            //  System.out.println("Checking for matrix map");
            // Go Look for a MatrixMapped method, instead.
 	    // Check if any arguments are of matrix type.
	    boolean hasArray = false;
            boolean[] isArrayArg = new boolean[argTypes.length];
	    Type[] newArgTypes = new Type[argTypes.length];
            for (int i = 0; i < argTypes.length; i++) {
                //     System.out.println("argType[" + i + "] = " + argTypes[i].getClass());
                if (argTypes[i] instanceof UnsizedMatrixType) {
                    hasArray = true;
                    newArgTypes[i] = ((UnsizedMatrixType)argTypes[i]).getElementType();
                    isArrayArg[i] = true;
                } else {
		    newArgTypes[i] = argTypes[i];
                    isArrayArg[i] = false;
		}
	    }

            if(hasArray) {
                CachedMethod mapCachedMethod =
                    findMethod(methodName, newArgTypes, type);
                if(!mapCachedMethod.isMissing()) {
                    cachedMethod = new MatrixMapCachedMethod(
                            methodName, argTypes, type+REAL,
                            mapCachedMethod, isArrayArg);
                }
            }
        }

        if(cachedMethod == null) {
            //  System.out.println("not found...");
            // If we haven't found anything by this point, then give
            // up...
            cachedMethod = new CachedMethod(methodName, argTypes,
                    null, null, type+MISSING);
        }

        // Add the method we found, or the placeholder for the missing method.
        add(cachedMethod);
        return cachedMethod;
    }

    /** Run method represented by this cachedMethod.  This includes any
     *  conversions necessary to turn token arguments into other arguments, and
     *  to convert the result back into a token.
     *  @param argValues An array of token objects that will be used
     *  as the arguments.
     *  @return The token result of the method invocation.
     *  @exception IllegalActionException If this cached method is
     *  MISSING, or the invoked method throws it.
     */
    public ptolemy.data.Token invoke(Object[] argValues)
            throws IllegalActionException {
        //     System.out.println("invoking " + toString() + " on:");
        //         for(int i = 0; i < argValues.length; i++) {
//             System.out.println("arg " + i + " = " + argValues[i]);
//         }

        if(isMissing()) {
            throw new IllegalActionException("A method compatible with "
                    + toString() + " cannot be found");
        }

	Object result = null;

        int type = getType();
        Method method = getMethod();

        if (isMethod()) {
            int num = argValues.length;
            Object[] methodArgValues = new Object[num - 1];
	    if (num == 1)
		methodArgValues = null;
            for (int i = 1; i < num; i++) {
                methodArgValues[i-1] = _conversions[i-1].convert(
                        (ptolemy.data.Token)argValues[i]);
            }
     //        for(int i = 0; i < num - 1; i++) {
//                 System.out.println("Convertedarg " + i + " = " + methodArgValues[i] + " class = " + methodArgValues[i].getClass());
//             }
            try {
                result = method.invoke(argValues[0], methodArgValues);
            } catch (InvocationTargetException ex) {
                ex.printStackTrace();
                throw new IllegalActionException(null, ex.getCause(),
                        "Error invoking method " + method + " on object " +
                        argValues[0] + "\n");
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new IllegalActionException(null, ex,
                        "Error invoking method " + method + " on object " +
                        argValues[0] + "\n");
            }
            return ASTPtFunctionNode.convertJavaTypeToToken(result);
        } else if(isFunction()) {
            int num = argValues.length;
            Object[] methodArgValues = new Object[num];
	    if (num == 0)
		methodArgValues = null;
            for (int i = 0; i < num; i++) {
                //          System.out.println("Conversion = " + _conversions[i]);
                methodArgValues[i] = _conversions[i].convert(
                        (ptolemy.data.Token)argValues[i]);
            }
      //       for(int i = 0; i < num; i++) {
//                 System.out.println("Convertedarg " + i + " = " + methodArgValues[i] + " class = " + methodArgValues[i].getClass());
//             }
            try {
                result = method.invoke(method.getDeclaringClass(),
                        methodArgValues);
            } catch (InvocationTargetException ex) {
                throw new IllegalActionException(null, ex.getCause(),
                        "Error invoking function " + method + "\n");
            } catch (Exception ex) {
                throw new IllegalActionException(null, ex,
                        "Error invoking function " + method + "\n");
            }
            return ASTPtFunctionNode.convertJavaTypeToToken(result);
        }
        throw new IllegalActionException("Cannot invoke function "
                + method +  " that is not simple function or method");
    }

    /** Returns the CONSTRUCTED, MISSING, METHOD, FUNCTION type of this.
     */
    public int getType() {
        return _type;
    }

    /** Returns the method associated with this REAL CachedMethod, null
     *  otherwise.
     */
    public Method getMethod() {
        return _method;
    }

    /** Return a string representation.
     */
    public String toString() {
        int initialArg = 0;
        StringBuffer buffer = new StringBuffer();
        if(isMethod()) {
            initialArg = 1;
            buffer.append(_argTypes[0].toString());
            buffer.append(".");
        }
        buffer.append(_methodName);
        buffer.append("(");
        for (int i = initialArg; i < _argTypes.length; i++) {
            if (i == initialArg) {
                buffer.append(_argTypes[i].toString());
            } else {
                buffer.append(", " + _argTypes[i].toString());
            }
        }
        buffer.append(")");
        return buffer.toString();
    }

    /** Add the CachedMethod to the cache.
     */
    public static void add(CachedMethod cachedMethod) {
        _cachedMethods.put(cachedMethod, cachedMethod);
    }

    /** Clear the cache - restarts the search of registered classes for methods
     *  and the rebuilding of the cache.
     */
    public static void clear() {
        _cachedMethods.clear();
    }

    /** Return the CachedMethod that corresponds to methodName and
     *  argTypes if it had been cached previously.
     */
    public static CachedMethod get(String methodName, Type[] argTypes,
            int type) {
        CachedMethod key = new CachedMethod(methodName, argTypes, null, null, type);
        // System.out.println("findMethod:" + key);
        CachedMethod method = (CachedMethod)_cachedMethods.get(key);
        return method;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

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
    protected static Method _polymorphicGetMethod(Class library,
            String methodName, Type[] argTypes,
            ArgumentConversion[] conversions) {
        while (library != null) {
            // We want to ascend the class hierarchy in a controlled way
            // so we use getDeclaredMethods() and getSuperclass()
            // instead of getMethods().  Note that this approach has the
            // side effect that additional methods (not only public) are
            // accessible.
            Method[] methods;
            try {
                methods = library.getDeclaredMethods();
            } catch (SecurityException security) {
                // We are in an applet.
                // This hack will likely only work for java.lang.Math.cos()
                methods = library.getMethods();
            }
            for (int i = 0; i < methods.length; i++) {
                // Check the name.
                if (!methods[i].getName().equals(methodName)) continue;

                Class[] arguments = methods[i].getParameterTypes();
                int actualArgCount;
                if(argTypes == null) {
                    actualArgCount = 0;
                } else {
                    actualArgCount = argTypes.length;
                }
                // Check the number of arguments.
                if (arguments.length != actualArgCount) continue;

                // Check the compatability of arguments.
                boolean match = true;
                for (int j = 0; j < arguments.length && match; j++) {
                    ArgumentConversion conversion =
                        _getConversion(arguments[j], argTypes[j]);
                    match = match && (conversion != IMPOSSIBLE);
                    conversions[j] = conversion;
                }
                if (match) {
                    return methods[i];
                }
            }
            library = library.getSuperclass();
        }
        return null;
    }

    /** Return true if an instance of actual can be used as an argument to
     *  a formal method argument of class formal.
     */
    protected static ArgumentConversion _getConversion(
            Class formal, Type actual) {
        // No conversion necessary.
        if(formal.isAssignableFrom(actual.getTokenClass()))
            return IDENTITY;

        // ArrayTokens can be converted to Token[]
        if(actual instanceof ArrayType &&
                formal.isArray() &&
                formal.getComponentType().isAssignableFrom(
                        ptolemy.data.Token.class))
            return ARRAYTOKEN;
        try {
            // Tokens can be converted to native types.
            if(formal.isAssignableFrom(
                       ASTPtFunctionNode.convertTokenTypeToJavaType(actual))) {
                return NATIVE;
            }
        } catch (IllegalActionException ex) {
            // Ignore..
            ex.printStackTrace();
        }
        return IMPOSSIBLE;
    }

    public static class ArgumentConversion {
        private ArgumentConversion(int type) {
            _type = type;
        }

        public int getType() {
            return _type;
        }

        public Object convert(ptolemy.data.Token input)
                throws IllegalActionException {
            throw new IllegalActionException(
                    "Cannot convert argument token " + input);
        }

        public String toString() {
            return "Conversion " + _type;
        }

        private int _type;
    }

    public static final ArgumentConversion IMPOSSIBLE = new ArgumentConversion(0);
    public static final ArgumentConversion IDENTITY = new ArgumentConversion(1) {
            public Object convert(ptolemy.data.Token input)
                    throws IllegalActionException {
                // The do nothing conversion.
                return input;
            }
        };
    public static final ArgumentConversion ARRAYTOKEN = new ArgumentConversion(2) {
            public Object convert(ptolemy.data.Token input)
                    throws IllegalActionException {
                // Convert ArrayToken to Token[]
                return ((ArrayToken)input).arrayValue();
            }
        };
    public static final ArgumentConversion NATIVE = new ArgumentConversion(3) {
            public Object convert(ptolemy.data.Token input)
                    throws IllegalActionException {
                // Convert tokens to native types.
                return ASTPtFunctionNode.convertTokenToJavaType(input)[0];
            }
        };

    // A cached method that converts the base as well.  This allows us to
    // invoke methods on, e.g. The ptolemy.math.Complex class and the
    // ptolemy.math.FixPoint class that are inside the corresponding tokens.
    public static class BaseConvertCachedMethod extends CachedMethod {
        public BaseConvertCachedMethod(
                String methodName, Type[] argTypes,
                Method method, ArgumentConversion baseConversion,
                ArgumentConversion[] conversions, int type) {
            super(methodName, argTypes, method, conversions, type);
            _baseConversion = baseConversion;
        }
        public ArgumentConversion getBaseConversion() {
            return _baseConversion;
        }
        public ptolemy.data.Token invoke(Object[] argValues)
                throws IllegalActionException {
            argValues[0] = _baseConversion.convert(
                    (ptolemy.data.Token)argValues[0]);
            return super.invoke(argValues);
        }
        private ArgumentConversion _baseConversion;
    }

    // A cached method that implements a simple array map for
    // some elements.
    public static class ArrayMapCachedMethod extends CachedMethod {
        public ArrayMapCachedMethod(
                String methodName, Type[] argTypes, int type,
                CachedMethod cachedMethod, boolean[] reducedArgs) {
            super(methodName, argTypes, null, null, type);
            _cachedMethod = cachedMethod;
            _reducedArgs = reducedArgs;
        }

        /** Run method represented by this cachedMethod.  This
         *  includes any conversions necessary to turn token arguments
         *  into other arguments, and to convert the result back into
         *  a token.
         *  @param argValues An array of token objects that will be used
         *  as the arguments.
         *  @return The token result of the method invocation.
         *  @exception IllegalActionException If the invoked method
         *  throws it.
         */
        public ptolemy.data.Token invoke(Object[] argValues)
            throws IllegalActionException {

            Object result = null;

            int dim = 0;
            // Check the argument lengths.
            for(int i = 0; i < argValues.length; i++) {
                if(_reducedArgs[i]) {
                    if(argValues[i] instanceof ArrayToken) {
                        ArrayToken arrayToken = (ArrayToken)argValues[i];
                        if(dim != 0 && arrayToken.length() != dim) {
                            throw new IllegalActionException(
                                    "Argument " + i +
                                    " is a reducible arrayToken that " +
                                    "does not have compatible length!");
                        } else {
                            dim = arrayToken.length();
                        }
                    } else {
                        throw new IllegalActionException(
                                "Argument " + i + " is not an instance of " +
                                "ArrayToken!");
                    }
                }
            }

            // Collect the not reducible args.
            Object[] subArgs = (Object[]) argValues.clone();
            ptolemy.data.Token[] tokenArray = new ptolemy.data.Token[dim];

            for(int j = 0; j < dim; j++) {
                for(int i = 0; i < argValues.length; i++) {
                    if(_reducedArgs[i]) {
                        subArgs[i] = ((ArrayToken)argValues[i]).getElement(j);
                    }
                }
                tokenArray[j] = _cachedMethod.invoke(subArgs);
            }

            return new ArrayToken(tokenArray);
        }

        public Type getReturnType() throws IllegalActionException {
            if(isMissing()) {
                throw new IllegalActionException(
                        "The return type of the method "
                        + toString() + " cannot be determined because "
                        + "no matching method was found.");
            }
            Type elementType = _cachedMethod.getReturnType();
            return new ArrayType(elementType);
        }

        private CachedMethod _cachedMethod;
        private boolean[] _reducedArgs;
    }

    // A cached method that implements a simple matrix map for
    // some elements.
    public static class MatrixMapCachedMethod extends CachedMethod {
        public MatrixMapCachedMethod(
                String methodName, Type[] argTypes, int type,
                CachedMethod cachedMethod, boolean[] reducedArgs) {
            super(methodName, argTypes, null, null, type);
            _cachedMethod = cachedMethod;
            _reducedArgs = reducedArgs;
        }

        /** Run method represented by this cachedMethod.  This
         *  includes any conversions necessary to turn token arguments
         *  into other arguments, and to convert the result back into
         *  a token.
         *  @param argValues An array of token objects that will be used
         *  as the arguments.
         *  @return The token result of the method invocation.
         *  @exception IllegalActionException If the invoked method
         *  throws it.
         */
        public ptolemy.data.Token invoke(Object[] argValues)
            throws IllegalActionException {

            Object result = null;

            int xdim = 0, ydim = 0;
            // Check the argument lengths.
            for(int i = 0; i < argValues.length; i++) {
                if(_reducedArgs[i]) {
                    if(argValues[i] instanceof MatrixToken) {
                        MatrixToken matrixToken = (MatrixToken)argValues[i];
                        if(xdim != 0 && ydim != 0 &&
                                (matrixToken.getRowCount() != ydim ||
                                 matrixToken.getColumnCount() != xdim)) {
                            throw new IllegalActionException(
                                    "Argument " + i +
                                    " is a reducible matrixToken that " +
                                    "does not have compatible size!");
                        } else {
                            ydim = matrixToken.getRowCount();
                            xdim = matrixToken.getColumnCount();
                        }
                    } else {
                        throw new IllegalActionException(
                                "Argument " + i + " is not an instance of " +
                                "MatrixToken!");
                    }
                }
            }

            // Collect the not reducible args.
            Object[] subArgs = (Object[]) argValues.clone();
            ptolemy.data.Token[] tokenArray =
                new ptolemy.data.Token[xdim*ydim];

            int pos = 0;
            for(int j = 0; j < ydim; j++) {
            for(int k = 0; k < xdim; k++) {
                for(int i = 0; i < argValues.length; i++) {
                    if(_reducedArgs[i]) {
                        subArgs[i] = ((MatrixToken)argValues[i]).getElementAsToken(j, k);
                    }
                }
                tokenArray[pos++] = _cachedMethod.invoke(subArgs);
            }
            }

            return MatrixToken.create(tokenArray, ydim, xdim);
        }

        public Type getReturnType() throws IllegalActionException {
            if(isMissing()) {
                throw new IllegalActionException(
                        "The return type of the method "
                        + toString() + " cannot be determined because "
                        + "no matching method was found.");
            }
            Type elementType = _cachedMethod.getReturnType();
            return UnsizedMatrixType.getMatrixTypeForElementType(elementType);
        }
        private CachedMethod _cachedMethod;
        private boolean[] _reducedArgs;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private String _methodName;
    private Type[] _argTypes;
    private Method _method;
    private ArgumentConversion[] _conversions;
    private int _hashcode;
    private int _type;
    private static Hashtable _cachedMethods = new Hashtable();
}
