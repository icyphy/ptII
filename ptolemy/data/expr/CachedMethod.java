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
 import ptolemy.kernel.util.InternalErrorException;
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
/**

An instance of this class represents a method or function that is invoked
by the Ptolemy II expression evaluator.  Instances of this class are
created by the findMethod() method and then stored in a hash table.
Repeated calls to findMethod() for the same method will return the
instance from the hash table, which is considerably faster than finding
the method again.  Finding methods is particularly expensive for
polymorphic methods and methods that take native types.
<p>
The functions represented by instances of this class are static methods
of classes that are registered with the parser (using
PtParser.registerFunctionClass()).  The methods represented
by instances of this class are methods of a Token and its subclasses.
<p>
Instances of this class have a type returned by getType(), which 
is a bitwise OR of either {@link #REAL} or {@link #MISSING} with
either {@link #FUNCTION} or {@link #METHOD}.  REAL indicates that
the method or function has been searched for and found, while
MISSING indicates that it has been searched for and not found.
FUNCTION indicates that it is a function (a static method of a
class registered with the parser), while METHOD indicates that it
is a method of a Token.
<p>
The cache is cleared by PtParser.registerFunctionClass() so that any
changes to the registered function classes cause the cache to be
re-generated.
<p>
Note that this class maintains the cache totally using instances of
ptolemy.data.type.Type.  While this is somewhat constraining since it
prevents using this class for reflecting things that are not methods
on ptolemy tokens, it makes it much easier to maintain the cache,
perform coherent type conversions on invocation, and generate code from
the expression language.

@author Zoltan Kemenczy, Research in Motion Limited., Steve Neuendorffer, Edward Lee
@version $Id$
@since Ptolemy II 2.0
@see ptolemy.data.expr.ASTPtFunctionNode
@see ptolemy.data.expr.PtParser
*/

public class CachedMethod {

    // FIXME: should the constructor be private? The correct entry point
    // is findMethod().
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
    ////                         public methods                    ////

    // FIXME: Should this be private?
    /** Add the specified instance of this class to the cache.
     *  @param cachedMethod The instance to add to the cache.
     */
    public static void add(CachedMethod cachedMethod) {
        _cachedMethods.put(cachedMethod, cachedMethod);
    }

    /** Clear the cache.
     */
    public static void clear() {
        _cachedMethods.clear();
    }

    /** Return true if the argument is an instance of CachedMethod
     *  that represents the same method or function as this instance.
     *  Note that if this returns true, then both this instance and
     *  the argument will have the same hashcode, as required by Hashtable.
     *  @param arg The object to compare to.
     *  @return True if the argument represents the same method or function.
     */
    public boolean equals(Object arg) {
        if (arg == this) {
            return true;
        }
        if(!(arg instanceof CachedMethod)) {
            return false;
        }
        CachedMethod argMethod = (CachedMethod)arg;
        if (!_methodName.equals(argMethod._methodName)) {
            return false;
        }
        if ((_type & (FUNCTION+METHOD)) !=
                (argMethod._type & (FUNCTION+METHOD))) {
            return false;
        }
        if (_argTypes.length != argMethod._argTypes.length) {
            return false;
        }
        for (int i = 0; i < _argTypes.length; i++) {
            if (!_argTypes[i].equals(argMethod._argTypes[i])) {
                return false;
            }
        }
        return true;
    }

    /** Return the conversions the are applied to the arguments of this
     *  function or method.
     *  @return The conversions applied to the arguments.
     */
    public ArgumentConversion[] getConversions() {
        return _conversions;
    }

    /** Return the type of the token that results from an invocation
     *  of this method.
     *  @exception IllegalActionException If this method or function
     *   was not found (it is MISSING).
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

    /** Return the hash code calculated when this was constructed.
     *  This ensures that if two instances of this class are equal (as
     *  determined by the equals() method), then they have the same
     *  hash code.
     *  @return A hash code.
     */
    public int hashCode() {
        return _hashcode;
    }

    /** Find method or function with the specified name and argument types.
     *  The last argument is either METHOD or FUNCTION to distinguish the
     *  two cases.  For the METHOD case, the first argument type is class
     *  on which the method is to be invoked.  For the FUNCTION case, the
     *  function is a static method of a registered class.
     *  This method attempts to find the specified function in the cache,
     *  and searches the registered classes only if the function is not
     *  in the cache.
     *  @param methodName The method or function name.
     *  @param argTypes The argument types, including as the first element
     *   the type of object on which the method is invoked, if this is a
     *   method invocation.
     *  @param type FUNCTION or METHOD.
     *  @return A function or method.
     */
    public static CachedMethod findMethod(
            String methodName,
            Type[] argTypes,
            int type)
            throws IllegalActionException {
        //  System.out.println("findMethod(" + methodName + ")");
        // Check to see if there is a cache already.
        CachedMethod cachedMethod = get(methodName, argTypes, type);
        if (cachedMethod != null) {
            // System.out.println("in cache");
            return cachedMethod;
        }
        // System.out.println("not in cache");

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
                destTokenClass = ASTPtFunctionNode
                        .convertTokenTypeToJavaType(argTypes[0]);

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
            // Keep track of multiple matches, to try to find the
            // most specific one.
            Method preferredMethod = null;
            ArgumentConversion[] preferredConversions = null;
            while (allClasses.hasNext() && cachedMethod == null) {
                Class nextClass = (Class)allClasses.next();
                // System.out.println("Examining registered class: "
                //        + nextClass);
                try {
                    Method method = _polymorphicGetMethod
                            (nextClass, methodName, argTypes, conversions);
                    if(method != null) {
                        // System.out.println("Found match: " + method);
                        // Compare to previous match, if there has
                        // been one.
                        if (preferredMethod == null
                                || _areConversionsPreferable(
                                conversions,
                                method.getParameterTypes(),
                                preferredConversions,
                                preferredMethod.getParameterTypes())) {
                            // Either there is no previous match,
                            // or the current match is preferable
                            // or equivalent to the previous match.
                            preferredMethod = method;
                            preferredConversions = (ArgumentConversion[])
                                    conversions.clone();
                        }
                    }
                } catch (SecurityException security) {
                    // If we are running under an Applet, then we
                    // may end up here if, for example, we try
                    // to invoke the non-existent quantize function on
                    // java.lang.Math.
                }
            }
            if (preferredMethod != null) {
                // System.out.println("*** Chosen method: "
                //        + preferredMethod);
                // System.out.println("*** Chosen conversions: "
                //        + preferredConversions[0]);
                cachedMethod = new CachedMethod(methodName, argTypes,
                        preferredMethod, preferredConversions, type+REAL);
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
                // System.out.println("argType[" + i + "] = "
                //         + argTypes[i].getClass());
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
            // System.out.println("Checking for matrix map");
            // Go Look for a MatrixMapped method, instead.
            // Check if any arguments are of matrix type.
            boolean hasArray = false;
            boolean[] isArrayArg = new boolean[argTypes.length];
            Type[] newArgTypes = new Type[argTypes.length];
            for (int i = 0; i < argTypes.length; i++) {
                // System.out.println("argType[" + i + "] = "
                //        + argTypes[i].getClass());
                if (argTypes[i] instanceof UnsizedMatrixType) {
                    hasArray = true;
                    newArgTypes[i] = ((UnsizedMatrixType)argTypes[i])
                            .getElementType();
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
            // System.out.println("not found...");
            // If we haven't found anything by this point, then give
            // up...
            cachedMethod = new CachedMethod(methodName, argTypes,
                    null, null, type+MISSING);
        }

        // Add the method we found, or the placeholder for the missing method.
        add(cachedMethod);
        return cachedMethod;
    }

    /** Return the CachedMethod that corresponds to methodName and
     *  argTypes if it had been cached previously.
     */
    public static CachedMethod get(
            String methodName, Type[] argTypes, int type) {
        CachedMethod key = new CachedMethod(
                   methodName, argTypes, null, null, type);
        // System.out.println("findMethod:" + key);
        CachedMethod method = (CachedMethod)_cachedMethods.get(key);
        return method;
    }

    /** Return the type of this class, which is one of
     *  REAL or MISSING or'ed with one of METHOD or FUNCTION.
     *  @return The type of this class.
     */
    public int getType() {
        return _type;
    }

    /** Return the method or function associated with this instance, or null
     *  if none has been found.
     *  @return The method associated with this instance.
     */
    public Method getMethod() {
        return _method;
    }

    /** Apply the method or function represented by this object to
     *  the specified arguments.  This method performs any necessary
     *  conversions on token arguments, and, if necessary,
     *  converts the returned value into a token.
     *  @param argValues An array of Token objects that will be used
     *   as the arguments.
     *  @return The result of the method invocation, as a Token.
     *  @exception IllegalActionException If this cached method is
     *   MISSING, or the invoked method throws it.
     */
    public ptolemy.data.Token invoke(Object[] argValues)
            throws IllegalActionException {
        // System.out.println("invoking " + toString() + " on:");
        // for(int i = 0; i < argValues.length; i++) {
        //     System.out.println("arg " + i + " = " + argValues[i]);
        // }

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
            // for(int i = 0; i < num - 1; i++) {
            //    System.out.println("Convertedarg " + i + " = "
            //            + methodArgValues[i] + " class = "
            //            + methodArgValues[i].getClass());
            // }
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
                // System.out.println("Conversion = " + _conversions[i]);
                methodArgValues[i] = _conversions[i].convert(
                        (ptolemy.data.Token)argValues[i]);
            }
            // for(int i = 0; i < num; i++) {
            //    System.out.println("Convertedarg " + i + " = "
            //           + methodArgValues[i] + " class = "
            //           + methodArgValues[i].getClass());
            // }
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

    /** Return true if this instance represents a function (vs. a method).
     *  @return True if this instance represents a function.
     */
    public boolean isFunction() {
        return (_type & FUNCTION) == FUNCTION;
    }

    /** Return true if this instance represents a method (vs. a function).
     *  @return True if this instance represents a method.
     */
    public boolean isMethod() {
        return (_type & METHOD) == METHOD;
    }

    /** Return true if the search for the method or function represented
     *  by this object turned up nothing.
     *  @return True if there is no such method.
     */
    public boolean isMissing() {
        return (_type & MISSING) == MISSING;
    }

    public boolean isReal() {
        return (_type & REAL) == REAL;
    }

    /** Return a string representation.
     *  @return A string representation.
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

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////
    
    /** A "missing" method or function is one that was not found. */
    public static final int MISSING = 1;

    /** A method or function that is "real" is one that has been
     *  found with the specified signature.
     */
    public static final int REAL = 4;

    /** Indicator of a function (vs. method). */
    public static final int FUNCTION = 8;

    /** Indicator of a method (vs. function). */
    public static final int METHOD = 16;

    // Note that conversions are ordered by preference..  IMPOSSIBLE is the
    // least preferable conversion, and has type of zero.

    /** Impossible argument conversion. */
    public static final ArgumentConversion
            IMPOSSIBLE = new ArgumentConversion(0);

    /** Conversion from an ArrayToken to a Token array (Token[]). */
    public static final ArgumentConversion
            ARRAYTOKEN = new ArgumentConversion(1) {
        public Object convert(ptolemy.data.Token input)
                throws IllegalActionException {
            // Convert ArrayToken to Token[]
            return ((ArrayToken)input).arrayValue();
        }
    };

    /** Conversion from tokens to Java native types. */
    public static final ArgumentConversion
            NATIVE = new ArgumentConversion(2) {
        public Object convert(ptolemy.data.Token input)
                throws IllegalActionException {
            // Convert tokens to native types.
            return ASTPtFunctionNode.convertTokenToJavaType(input)[0];
        }
    };

    /** Identity conversion.  Does nothing. */
    public static final ArgumentConversion
            IDENTITY = new ArgumentConversion(3) {
        public Object convert(ptolemy.data.Token input)
                throws IllegalActionException {
            // The do nothing conversion.
            return input;
        }
    };

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return true if the conversions in the first argument are
     *  preferable to the conversions in the third argument, for methods
     *  with argument types given by the second and fourth arguments.
     *  To return true, every conversion in the first method
     *  must be preferable or equal to conversions in the second.
     *  The two arguments are required to have the same length, or
     *  else an InternalErrorException will be thrown.
     *  @param conversions1 The first set of conversions.
     *  @param arguments1 The arguments of the first method.
     *  @param conversions2 The second set of conversions.
     *  @param arguments2 The arguments of the second method.
     *  @return True if the first set of conversions is preferable
     *   to the second.
     */
    protected static boolean _areConversionsPreferable(
            ArgumentConversion[] conversions1,
            Class[] arguments1,
            ArgumentConversion[] conversions2,
            Class[] arguments2) {
        if (conversions1.length != conversions2.length) {
            throw new InternalErrorException(
                    "Conversion arrays have to have the same length.");
        }
        for(int j = 0; j < conversions1.length; j++) {
            if(conversions2[j].isPreferableTo(conversions1[j])) {
                // Found one conversion where the second argument is
                // preferable.  That is enough to return false.
                return false;
            } else if(conversions2[j].equals(conversions1[j])) {
                // Conversions are the same.
                // Use the types of the arguments to get more specific.
                Class class1 = arguments1[j];
                Class class2 = arguments2[j];
                try {
                    Type type1 = ASTPtFunctionNode
                            .convertJavaTypeToTokenType(class1);
                    Type type2 = ASTPtFunctionNode
                            .convertJavaTypeToTokenType(class2);
                    if(TypeLattice.compare(type2, type1)
                            == ptolemy.graph.CPO.LOWER) {
                        // Found one conversion where the second method
                        // is preferable. This is enough to return false.
                        // An argument that is lower is the lower in the
                        // type lattice is preferable because it is
                        // more specific.
                        return false;
                    }
                } catch (IllegalActionException ex) {
                    // Failed to find a token type, so can't perform
                    // the comparison.  Ignore the error so that it remains
                    // possible to return true.  This allows the latest
                    // matching method to be used.
                }
            }
        }
        // No argument was found where the second is preferable,
        // so we return true.
        return true;
    }

    /** Return the first method in the specified library that has
     *  the specified name and can be invoked with the specified
     *  argument types. The last argument is an array that is populated
     *  with the conversions that will be required to invoke this method.
     *  It is arguable that it should return the most specific method
     *  that it finds, but it turns out that this is difficult
     *  to define.  So it simply returns the first match.
     *  It returns null if there is no match.
     *  Regrettably, the getMethod() method in the java Class does not
     *  support polymorphism.  In particular, it does not recognize a method
     *  if the class you supply for an argument type is actually derived
     *  from the class in the method signature.  So we have to reimplement
     *  this here.
     *  @param library A class with methods giving a function library.
     *  @param methodName The name of the method.
     *  @param argTypes The types of the arguments.
     *  @param conversions An array of the same length as <i>argTypes</i>
     *   that will be populated by this method with the conversions to
     *   use for the arguments.
     */
    protected static Method _polymorphicGetMethod(Class library,
            String methodName, Type[] argTypes,
            ArgumentConversion[] conversions) {
        Method matchedMethod = null;
        ArgumentConversion[] matchedConversions = 
                new ArgumentConversion[conversions.length];
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

                // System.out.println("checking method " + methods[i]);

                // Check the compatability of arguments.
                boolean match = true;
                for (int j = 0; j < arguments.length && match; j++) {
                    ArgumentConversion conversion =
                            _getConversion(arguments[j], argTypes[j]);
                    // System.out.println("formalType is "
                    //       + arguments[j] + " " + arguments[j].getName());
                    // System.out.println("actualType is " + argTypes[j]
                    //       + " " + argTypes[j].getClass().getName());
                    match = match && (conversion != IMPOSSIBLE);
                    conversions[j] = conversion;
                }
                // If there was a previous match, then check to see
                // which one is preferable.
                if (match && matchedMethod != null) {
                    // Set match to false if previously found match is
                    // preferable to this one.  matchedConversions is
                    // the set of previously found conversions.
                    match = _areConversionsPreferable(
                            conversions, arguments,
                            matchedConversions,
                            matchedMethod.getParameterTypes());
                }
                if (match) {
                    // If still a match, then remember the method for later,
                    // so it can be checked against any other match found.
                    matchedMethod = methods[i];
                    System.arraycopy(conversions, 0,
                            matchedConversions, 0, actualArgCount);
                }
            }
            library = library.getSuperclass();
        }
        System.arraycopy(matchedConversions, 0,
                conversions, 0, conversions.length);
        return matchedMethod;
    }

    /** Return a conversion to convert the second argument into the class
     *  given by the first argument. If no such conversion is possible, then
     *  the returned conversion is IMPOSSIBLE.
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
            //          ex.printStackTrace();
        }
        try {
            // We have to do this because Java is stupid and doesn't
            // give us a way to tell if primitive arguments are
            // acceptable
            if(formal.isPrimitive()) {
                Type type = 
                    ASTPtFunctionNode.convertJavaTypeToTokenType(formal);
                if(ptolemy.graph.CPO.LOWER ==
                        TypeLattice.compare(actual, type)) {
                    return NATIVE;
                }
            }
        } catch (IllegalActionException ex) {
            // Ignore..
            //          ex.printStackTrace();
        }
        return IMPOSSIBLE;
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

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** Class representing an argument conversion.
     *  Instances of this class are returned by getConversions().
     */
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

        public boolean isPreferableTo(ArgumentConversion conversion) {
            return _type > conversion.getType();
        }

        public String toString() {
            return "Conversion " + _type;
        }

        private int _type;
    }

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
                            subArgs[i] = ((MatrixToken)argValues[i])
                                    .getElementAsToken(j, k);
                        }
                    }
                    tokenArray[pos++] = _cachedMethod.invoke(subArgs);
                }
            }

            return MatrixToken.createMatrix(tokenArray, ydim, xdim);
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
}
