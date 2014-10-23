/* CachedMethod provides methods for reflecting methods based on token types.

 Copyright (c) 1998-2014 The Regents of the University of California and
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


 */
package ptolemy.data.expr;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Hashtable;
import java.util.Iterator;

import ptolemy.data.ArrayToken;
import ptolemy.data.MatrixToken;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.MatrixType;
import ptolemy.data.type.Type;
import ptolemy.data.type.TypeLattice;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;

///////////////////////////////////////////////////////////////////
//// CachedMethod

/**
 An instance of this class represents a method or function that is
 invoked by the Ptolemy II expression evaluator.  Instances of this
 class are returned by the static findMethod() method, and can be
 invoked by the apply() method.  This class is used by the expression
 language to find Java methods that are bound in expressions that use
 function application, i.e. an ASTPtFunctionApplicationNode, and in method
 invocation, i.e. an ASTPtMethodNode.

 <p> This class is used to represent two distinct types of Java methods
 that can be invoked.  The METHOD type corresponds to an instance
 method of a java class, invoked on an object of an appropriate class
 (the <i>base class</i>).  The FUNCTION type corresponds to a static
 method of a java class.  These types corresponds to the two distinct
 expression constructs that can be used to invoke Java methods.  The
 type of construct reflected can be queried using the
 getCachedMethodType() method, which returns either {@link #FUNCTION}
 or {@link #METHOD}.  Additionally, this class can be used to represent
 Java methods that were not found.  If the CachedMethod corresponds to
 an invokeable Java method, then the isValid() method will return true.
 CachedMethods that are not valid cannot be invoked by the invoke()
 method.

 <p> This class provides several services that distinguish it from
 Java's built-in reflection mechanism:
 <ol>
 <li> Methods are found based on name and the types of ptolemy token
 arguments, represented by instances of the ptolemy.data.type.Type
 base class.
 <li> FUNCTIONS are searched for in a set of classes registered with the
 parser.
 <li> METHODS are searched for a base class, and in all superclasses of
 the base class.
 <li> Found methods, represented by instances of this class, are cached
 and indexed to improve the speed of method lookup.  The cache is
 synchronized so that it can be safely accessed from multiple
 threads.
 <li> Allows for the possibility of several automatic conversions that
 increase the applicability of single methods
 </ol>

 <p> The automatic conversions that are allowed on the arguments of
 reflected Java methods can be particularly tricky to understand.  The
 findMethod() method is fairly aggressive about finding valid methods
 to invoke.  In particular, given a set of arguments with token types,
 the findMethod() method might return a cached method that:

 <ol>
 <li> Accepts token arguments of exactly the same type.
 <li> Accepts token arguments that are of a type that the given types can
 be automatically converted to, as determined by the Ptolemy type
 lattice.
 <li> Accepts the corresponding Java native type of either of the first
 two cases, i.e. an IntToken argument may reflect a method that
 accepts a Java int.
 <li> Accepts a corresponding Java array type, if the argument type is an
 ArrayType.
 <li> Accepts a corresponding Java array of array type, if the argument
 type is a MatrixType.
 </ol>

 The underlying conversions are implemented by the {@link
 ConversionUtilities} class, which has more specific documentation the
 underlying conversions.  The inverse of the same conversions are
 performed on the results of a Java method invocation, in order to
 convert the result back into a Ptolemy token.

 <p> Since there may be many methods that match a particular function
 application or method invocation, under the above conversions, the
 findMethod() method attempts to return the most specific Java method
 that can be called.  Generally speaking, conversions are preferred in
 the above order.  If one Java method is not clearly preferable to all
 others, then the findMethod() method will throw an exception.  This
 may happen if there are multiple functions defined with varying
 argument types.

 <p> Additionally, the findMethod() method may return a CachedMethod
 that automatically "maps" arrays and matrices over a scalar function.
 The result of invoking the CachedMethod is an array or matrix of
 whatever type is returned by the original function.

 <p> As an example of how this works, evaluation of the expression
 "fix([0.5, 0.1; 0.4, 0.3], 16, 1)" performs results in the invocation
 of the method named "fix" in the ptolemy.data.expr.FixPointFunctions
 that takes a Java double and two Java ints and returns an instance of
 ptolemy.math.FixPoint.  This function is invoked once for each element
 of the matrix (converting each DoubleToken into the corresponding
 double, and each IntToken into the corresponding int), and the results
 are packaged back into a 2x2 FixMatrixToken.

 <p> Additional classes to be searched for static methods can be added
 through the method registerFunctionClass() in PtParser.  This class
 assumes that new classes are added to the search path before models
 are constructed, and simply clears the internal cache and index when
 new classes are registered.

 @author Zoltan Kemenczy, Research in Motion Limited., Steve Neuendorffer, Edward Lee
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Green (neuendor)
 @Pt.AcceptedRating Yellow (neuendor)
 @see ptolemy.data.expr.ASTPtFunctionApplicationNode
 @see ptolemy.data.expr.PtParser
 */
public class CachedMethod {
    /** Construct a new CachedMethod.  Generally speaking, it is not
     *  necessary for any users of this class to invoke this method.
     *  The static findMethod() method finds the appropriate method
     *  for a given set of argument types and invokes this
     *  constructor to create a cached method.
     *
     *  @param methodName The name of the encapsulated method.
     *  @param argumentTypes An array of token types that can be passed to
     *  the method, subject to the given set of conversions.  For a
     *  FUNCTION, the number of argument types must be the same as the
     *  number of arguments to the given method.  For a METHOD, there
     *  is an additional type in the array (the first) corresponding
     *  to the type of object the method is getting invoked on.
     *  @param method The Java method that will be invoked by the
     *  invoke() method.  If this parameter is null, then the method is
     *  not valid and cannot be invoked.
     *  @param conversions An array of conversions that will convert
     *  arguments of the corresponding argument types to arguments
     *  that the method will accept.  If the method accepts Token
     *  arguments, then this array will contain IDENTITY_CONVERSION
     *  conversions.  This array must be the same size as the number
     *  of arguments to the method.
     *  @param type The type of the method.
     *  @exception IllegalActionException If the return type of the
     *  cached method cannot be determined.
     */
    protected CachedMethod(String methodName, Type[] argumentTypes,
            Method method, ArgumentConversion[] conversions, int type)
                    throws IllegalActionException {
        // Note clones for safety...
        _methodName = methodName;
        // Kepler (jdk1.4?) requires this cast
        _argumentTypes = argumentTypes.clone();
        _method = method;

        if (conversions != null) {
            // Kepler (jdk1.4?) requires this cast
            _conversions = conversions.clone();
        } else {
            _conversions = null;
        }

        _type = type;

        _returnType = null;

        // Compute the hashcode, based on the method name and argument
        // types.
        _hashcode = methodName.hashCode();

        for (Type argumentType : argumentTypes) {
            _hashcode += argumentType.hashCode();
        }

        // Determine the return type of the method, given our argument types.
        // Do this LAST, since invoking the type constraint method might throw
        // IllegalActionException, which we throw out of the constructor.
        if (_method != null) {
            // The default is to look at the return type of the method.
            Class returnClass = _method.getReturnType();
            _returnType = ConversionUtilities
                    .convertJavaTypeToTokenType(returnClass);

            // Check to see if there is a function that
            // provides a better return type.
            try {
                int args = _argumentTypes.length;
                Class[] typeArray = new Class[args];
                Class typeClass = Type.class;
                java.util.Arrays.fill(typeArray, typeClass);

                Method typeFunction = _method.getDeclaringClass().getMethod(
                        _methodName + "ReturnType", typeArray);

                // Invoke the function, and save the return type.
                try {
                    // Cast to (Object []) so as to avoid varargs warning.
                    _returnType = (Type) typeFunction.invoke(null,
                            (Object[]) _argumentTypes);
                } catch (IllegalAccessException ex) {
                    throw new RuntimeException(ex); // TODO
                } catch (InvocationTargetException ex) {
                    throw new RuntimeException(ex); // TODO
                }
            } catch (NoSuchMethodException ex) {
                // Ignore.  Just use the default return type above.
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clear the cache.  This is generally called by the PtParser class
     *  when new classes are registered to be searched.
     */
    public static void clear() {
        _cachedMethods.clear();
    }

    /** Return true if the argument is an instance of CachedMethod
     *  that represents the same method or function as this instance.
     *  Note that if this returns true, then both this instance and
     *  the argument will have the same hashcode, as required by the
     *  Object base class.
     *  @param object The object to compare to.
     *  @return True if the argument represents the same method or function.
     */
    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }

        if (!(object instanceof CachedMethod)) {
            return false;
        }

        CachedMethod cachedMethod = (CachedMethod) object;

        if (!_methodName.equals(cachedMethod._methodName)) {
            return false;
        }

        if ((_type & FUNCTION + METHOD) != (cachedMethod._type & FUNCTION
                + METHOD)) {
            return false;
        }

        if (_argumentTypes.length != cachedMethod._argumentTypes.length) {
            return false;
        }

        for (int i = 0; i < _argumentTypes.length; i++) {
            if (!_argumentTypes[i].equals(cachedMethod._argumentTypes[i])) {
                return false;
            }
        }

        return true;
    }

    /** Find a method or function with the specified name and argument types.
     *  The last argument is either METHOD or FUNCTION to distinguish the
     *  two cases.  For the METHOD case, the first argument type is the class
     *  on which the method is to be invoked.  For the FUNCTION case, the
     *  function is a static method of a registered class.
     *  This method attempts to find the specified function in the cache,
     *  and searches the registered classes only if the function is not
     *  in the cache.
     *
     *  <p> This method first attempts to resolve the function in the
     *  registered function classes by finding a method and a set of
     *  argument conversions that allow the method to be invoked on
     *  the given types.  If the above fails and at least one argument
     *  is an array type or matrix type, a map is attempted over those
     *  argument types and the registered function classes are
     *  searched again. This process is repeated until all arguments
     *  are scalars or a function signature match is found.
     *
     *  @param methodName The method or function name.
     *  @param argumentTypes The argument types, including as the first element
     *   the type of object on which the method is invoked, if this is a
     *   method invocation.
     *  @param type FUNCTION or METHOD.
     *  @return A cached method that is valid if a matching method was found.
     *  @exception IllegalActionException If the method cannot be found.
     */
    public static CachedMethod findMethod(String methodName,
            Type[] argumentTypes, int type) throws IllegalActionException {
        // Check to see if there is a cache already.
        CachedMethod cachedMethod = _getCachedMethod(methodName, argumentTypes,
                type);

        if (cachedMethod != null) {
            //    System.out.println("in cache");
            return cachedMethod;
        }

        //  System.out.println("not in cache");
        // First look for the method or function in the normal place.
        if (type == METHOD) {
            cachedMethod = _findMETHOD(methodName, argumentTypes);
        } else if (type == FUNCTION) {
            cachedMethod = _findFUNCTION(methodName, argumentTypes);
        } else {
            throw new IllegalActionException("Attempted to find a method "
                    + "with an invalid type = " + type);
        }

        // We didn't find in the normal place, so try unrolling
        // array and matrices.
        if (cachedMethod == null) {
            // System.out.println("Checking for array map");
            // Go Look for an ArrayMapped method, instead.
            // Check if any arguments are of array type.
            boolean hasArray = false;
            boolean[] isArrayArg = new boolean[argumentTypes.length];
            Type[] newArgTypes = new Type[argumentTypes.length];

            for (int i = 0; i < argumentTypes.length; i++) {
                // System.out.println("argType[" + i + "] = "
                //         + argumentTypes[i].getClass());
                if (argumentTypes[i] instanceof ArrayType) {
                    hasArray = true;
                    newArgTypes[i] = ((ArrayType) argumentTypes[i])
                            .getElementType();
                    isArrayArg[i] = true;
                } else {
                    newArgTypes[i] = argumentTypes[i];
                    isArrayArg[i] = false;
                }
            }

            if (hasArray) {
                CachedMethod mapCachedMethod = findMethod(methodName,
                        newArgTypes, type);

                if (mapCachedMethod.isValid()) {
                    cachedMethod = new ArrayMapCachedMethod(methodName,
                            argumentTypes, type, mapCachedMethod, isArrayArg);
                }
            }
        }

        if (cachedMethod == null) {
            // System.out.println("Checking for matrix map");
            // Go Look for a MatrixMapped method, instead.
            // Check if any arguments are of matrix type.
            boolean hasArray = false;
            boolean[] isArrayArg = new boolean[argumentTypes.length];
            Type[] newArgTypes = new Type[argumentTypes.length];

            for (int i = 0; i < argumentTypes.length; i++) {
                // System.out.println("argType[" + i + "] = "
                //        + argumentTypes[i].getClass());
                if (argumentTypes[i] instanceof MatrixType) {
                    hasArray = true;
                    newArgTypes[i] = ((MatrixType) argumentTypes[i])
                            .getElementType();
                    isArrayArg[i] = true;
                } else {
                    newArgTypes[i] = argumentTypes[i];
                    isArrayArg[i] = false;
                }
            }

            if (hasArray) {
                CachedMethod mapCachedMethod = findMethod(methodName,
                        newArgTypes, type);

                if (mapCachedMethod.isValid()) {
                    cachedMethod = new MatrixMapCachedMethod(methodName,
                            argumentTypes, type, mapCachedMethod, isArrayArg);
                }
            }
        }

        if (cachedMethod == null) {
            // System.out.println("not found...");
            // If we haven't found anything by this point, then give
            // up...  Store an invalid cached method, so we don't try
            // the same search any more.
            cachedMethod = new CachedMethod(methodName, argumentTypes, null,
                    null, type);
        }

        // Add the method we found, or the placeholder for the missing method.
        _addCachedMethod(cachedMethod);
        return cachedMethod;
    }

    /** Return the type of this class, which is one of METHOD or FUNCTION.
     *  @return The type of this class.
     */
    public int getCachedMethodType() {
        return _type;
    }

    /** Return the conversions the are applied to the arguments of
     *  this function or method.  Note that in most cases, it is not
     *  necessary to call this method, as the invoke() method provides
     *  all the necessary information.  It is provided for code, such
     *  as the code generator that need more than the usual amount of
     *  information about methods that have been found.
     *  @return The conversions applied to the arguments.
     */
    public ArgumentConversion[] getConversions() {
        return _conversions;
    }

    /** Return the method giving the operation associated with this
     *  object, or null if none was found.  Note that in most cases,
     *  it is not necessary to call this method, as the invoke()
     *  method provides all the necessary information.  It is provided
     *  for code, such as the code generator that need more than the
     *  usual amount of information about methods that have been
     *  found.
     *  @return The method associated with this instance.
     *  @exception IllegalActionException If the method was not found,
     *  or this class represents a method mapped over an array or
     *  matrix.
     */
    public Method getMethod() throws IllegalActionException {
        if (isValid()) {
            return _method;
        } else {
            throw new IllegalActionException("No method " + toString()
                    + " was found!");
        }
    }

    /** Return the type of the token that results from an invocation
     *  of this method.  Note that in most cases, it is not necessary
     *  to call this method, as the invoke() method provides all the
     *  necessary information.  It is provided for code, such as the
     *  code generator that need more than the usual amount of
     *  information about methods that have been found.
     *  @return The type of the token that results from an invocation of
     *  this method.
     *  @exception IllegalActionException If a method or function with
     *  the correct argument types was not found.
     */
    public Type getReturnType() throws IllegalActionException {
        if (_returnType == null) {
            throw new IllegalActionException("The return type of the method "
                    + toString() + " cannot be determined because "
                    + "no matching method was found.");
        }

        return _returnType;
    }

    /** Return the hash code.  This method is overridden to be
     *  consistent with the overridden equals method.
     *  @return A hash code.
     */
    @Override
    public int hashCode() {
        return _hashcode;
    }

    /** Apply the operation represented by this object to
     *  the specified arguments.  This method performs any necessary
     *  conversions on token arguments, and, if necessary,
     *  converts the returned value into a token.  This method may be
     *  overridden by derived classes to implement non-standard conversions.
     *  @param argValues An array of Token objects that will be used
     *  as the arguments.
     *  @return The result of the method invocation, as a Token.
     *  @exception IllegalActionException If this cached method is
     *   not valid, or the invoked method throws it.
     */
    public ptolemy.data.Token invoke(Object[] argValues)
            throws IllegalActionException {
        //     System.out.println("invoking " + getMethod().toString() + " on:");
        //         for (int i = 0; i < argValues.length; i++) {
        //             System.out.println("arg " + i + " = " + argValues[i]);
        //         }
        Object result = null;

        Method method = getMethod();

        if (isMethod()) {
            int num = argValues.length;
            Object[] methodArgValues = new Object[num - 1];

            if (num == 1) {
                methodArgValues = null;
            }

            for (int i = 1; i < num; i++) {
                methodArgValues[i - 1] = _conversions[i - 1]
                        .convert((ptolemy.data.Token) argValues[i]);
            }

            // for (int i = 0; i < num - 1; i++) {
            //    System.out.println("ConvertedArg " + i + " = "
            //            + methodArgValues[i] + " class = "
            //            + methodArgValues[i].getClass());
            // }
            try {
                result = method.invoke(argValues[0], methodArgValues);
            } catch (RuntimeException ex) {
                // Avoid mungeing runtime exceptions, since they really
                // are coding bugs.
                throw ex;
            } catch (InvocationTargetException ex) {
                throw new IllegalActionException(null, ex.getCause(),
                        "Error invoking method " + method + " on object "
                                + argValues[0] + "\n");
            } catch (Exception ex) {
                throw new IllegalActionException(null, ex,
                        "Error invoking method " + method + " on object "
                                + argValues[0] + "\n");
            }

            return ConversionUtilities.convertJavaTypeToToken(result);
        } else if (isFunction()) {
            int num = argValues.length;
            Object[] methodArgValues = new Object[num];

            if (num == 0) {
                methodArgValues = null;
            }

            for (int i = 0; i < num; i++) {
                // System.out.println("Conversion = " + _conversions[i]);
                methodArgValues[i] = _conversions[i]
                        .convert((ptolemy.data.Token) argValues[i]);
            }

            // for (int i = 0; i < num; i++) {
            //    System.out.println("ConvertedArg " + i + " = "
            //           + methodArgValues[i] + " class = "
            //           + methodArgValues[i].getClass());
            // }
            try {
                result = method.invoke(method.getDeclaringClass(),
                        methodArgValues);
            } catch (RuntimeException ex) {
                // Avoid mungeing runtime exceptions, since they really
                // are coding bugs.
                throw ex;
            } catch (InvocationTargetException ex) {
                throw new IllegalActionException(null, ex.getCause(),
                        "Error invoking function " + method + "\n");
            } catch (Exception ex) {
                throw new IllegalActionException(null, ex,
                        "Error invoking function " + method + "\n");
            }

            return ConversionUtilities.convertJavaTypeToToken(result);
        }

        throw new IllegalActionException("Cannot invoke function " + method
                + " that is not simple function or method");
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
     *  by this object found an invokeable method.
     *  @return True if a method was found.
     */
    public boolean isValid() {
        return _method != null;
    }

    /** Return a verbose description of the cached method being invoked.
     *  @return A verbose description of the cached method being invoked.
     */
    public String methodDescription() {
        if (isValid()) {
            return _method.toString();
        } else {
            return "INVALID METHOD!!!";
        }
    }

    /** Return a string representation.
     *  @return A string representation.
     */
    @Override
    public String toString() {
        int initialArg = 0;
        StringBuffer buffer = new StringBuffer();

        if (isMethod()) {
            initialArg = 1;
            buffer.append(_argumentTypes[0].toString());
            buffer.append(".");
        }

        buffer.append(_methodName);
        buffer.append("(");

        for (int i = initialArg; i < _argumentTypes.length; i++) {
            if (i == initialArg) {
                buffer.append(_argumentTypes[i].toString());
            } else {
                buffer.append(", " + _argumentTypes[i].toString());
            }
        }

        buffer.append(")");
        return buffer.toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** Indicator of a function (vs. method). */
    public static final int FUNCTION = 8;

    /** Indicator of a method (vs. function). */
    public static final int METHOD = 16;

    // Note that conversions are ordered by preference..
    // IMPOSSIBLE_CONVERSION is the least preferable conversion, and
    // has type of zero.

    /** Impossible argument conversion. */
    public static final ArgumentConversion IMPOSSIBLE_CONVERSION = new ArgumentConversion(
            0);

    /** Conversion from an ArrayToken to a Token array (Token[]). */
    public static final ArgumentConversion ARRAYTOKEN_CONVERSION = new ArgumentConversion(
            1) {
        @Override
        public Object convert(ptolemy.data.Token input)
                throws IllegalActionException {
            // Convert ArrayToken to Token[]
            return ((ArrayToken) input).arrayValue();
        }
    };

    /** Conversion up to a higher type has preference 2... */
    /** Conversion from tokens to Java native types. */
    public static final ArgumentConversion NATIVE_CONVERSION = new ArgumentConversion(
            3) {
        @Override
        public Object convert(ptolemy.data.Token input)
                throws IllegalActionException {
            // Convert tokens to native types.
            return ConversionUtilities.convertTokenToJavaType(input);
        }
    };

    /** Identity conversion.  Does nothing. */
    public static final ArgumentConversion IDENTITY_CONVERSION = new ArgumentConversion(
            4) {
        @Override
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
            ArgumentConversion[] conversions1, Class[] arguments1,
            ArgumentConversion[] conversions2, Class[] arguments2) {
        if (conversions1.length != conversions2.length) {
            throw new InternalErrorException(
                    "Conversion arrays have to have the same length.");
        }

        for (int j = 0; j < conversions1.length; j++) {
            //  System.out.println("comparing " + conversions1[j]);
            //  System.out.println("to        " + conversions2[j]);
            if (conversions2[j].isPreferableTo(conversions1[j])) {
                // Found one conversion where the second argument is
                // preferable.  That is enough to return false.
                // System.out.println("second arg (" + conversions2[j]
                //        + ") is preferable to " + conversions1[j]
                //        + ", returning false");
                return false;
            } else if (conversions2[j].equals(conversions1[j])) {
                // Conversions are the same.
                // Use the types of the arguments to get more specific.
                // System.out.println("conversions are the same");
                Class class1 = arguments1[j];
                Class class2 = arguments2[j];

                try {
                    Type type1 = ConversionUtilities
                            .convertJavaTypeToTokenType(class1);
                    Type type2 = ConversionUtilities
                            .convertJavaTypeToTokenType(class2);

                    if (TypeLattice.compare(type2, type1) == ptolemy.graph.CPO.LOWER) {
                        // Found one conversion where the second method
                        // is preferable. This is enough to return false.
                        // An argument that is lower is the lower in the
                        // type lattice is preferable because it is
                        // more specific.
                        //System.out.println("Found one conversion where "
                        //        + type2 + " is preferable to "
                        //        + type1 + " returning false");

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
        //System.out.println("No argument was found were the "
        //        + "second conversion is preferable, returning true");
        return true;
    }

    /** Return a conversion to convert the second argument into the class
     *  given by the first argument.
     *  @param formal The class to which the type shall be converted.
     *  @param actual The type to be converted.
     *  @return The best correct conversion, or IMPOSSIBLE_CONVERSION
     *  if no such conversion exists.
     */
    protected static ArgumentConversion _getConversion(Class formal, Type actual) {
        // No conversion necessary.
        if (formal.isAssignableFrom(actual.getTokenClass())) {
            return IDENTITY_CONVERSION;
        }

        // ArrayTokens can be converted to Token[]
        if (actual instanceof ArrayType
                && formal.isArray()
                && formal.getComponentType().isAssignableFrom(
                        ptolemy.data.Token.class)) {
            return ARRAYTOKEN_CONVERSION;
        }

        try {
            // Tokens can be converted to native types.
            if (formal.isAssignableFrom(ConversionUtilities
                    .convertTokenTypeToJavaType(actual))) {
                return NATIVE_CONVERSION;
            }
        } catch (IllegalActionException ex) {
            // Ignore..
            //          ex.printStackTrace();
        }

        try {
            // We have to do this because Java is stupid and doesn't
            // give us a way to tell if primitive arguments are
            // acceptable
            if (formal.isPrimitive() || formal.isArray()) {
                Type type = ConversionUtilities
                        .convertJavaTypeToTokenType(formal);

                if (ptolemy.graph.CPO.LOWER == TypeLattice
                        .compare(actual, type)) {
                    return new TypeArgumentConversion(type, NATIVE_CONVERSION);
                }
            }
        } catch (IllegalActionException ex) {
            // Ignore..
            //          ex.printStackTrace();
        } catch (InternalErrorException ex2) {
            // Ignore.

            // If formal is a char, then convertJavaTypeToTokenType(formal)
            // will throw an InternalErrorException.
            // One way to trigger this is to have an expression with
            // the value: "valueOf(input)".  What happens is that
            // String.valueOf(char) and String.valueOf(char[]) will be
            // checked, which causes convertTypeTypeToTokenType() to
            // throw the InternalErrorException.
            //new Exception("formal: " + formal
            //        + " formal.isPrimitive(): "
            //        + formal.isPrimitive()
            //        + " formal.isArray(): "
            //        + formal.isArray(), ex2).printStackTrace();
        }
        return IMPOSSIBLE_CONVERSION;
    }

    /** Return the first method in the specified class that has the
     *  specified name and can be invoked with the specified argument
     *  types.  The last argument is an array that is populated with
     *  the conversions that will be required to invoke this method.
     *  This method walks through all the superclasses of the given
     *  class, and returns the best match (resulting in the most
     *  preferable set of argument conversions) to the given argument
     *  types.  It returns null if there is no match.
     *  @param library A class to be searched.
     *  @param methodName The name of the method.
     *  @param argumentTypes The types of the arguments.
     *  @param conversions An array of the same length as <i>argumentTypes</i>
     *   that will be populated by this method with the conversions to
     *   use for the arguments.
     *  @return the first method in the specified class that has the
     *  specified name and can be invoked with the specified argument
     *  types.
     */
    protected static Method _polymorphicGetMethod(Class library,
            String methodName, Type[] argumentTypes,
            ArgumentConversion[] conversions) {
        // This method might appear to duplicate the operation of the
        // getMethod() method in java.lang.Class.  However, that class
        // does not support polymorphism, or traversal through
        // superclasses.  It is simpler to just walk the class
        // hierarchy ourselves.
        Method matchedMethod = null;
        ArgumentConversion[] matchedConversions = new ArgumentConversion[conversions.length];

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
                if (!methods[i].getName().equals(methodName)) {
                    continue;
                }

                Class[] arguments = methods[i].getParameterTypes();
                int actualArgCount;

                if (argumentTypes == null) {
                    actualArgCount = 0;
                } else {
                    actualArgCount = argumentTypes.length;
                }

                // Check the number of arguments.
                if (arguments.length != actualArgCount) {
                    continue;
                }

                //  System.out.println("checking method " + methods[i]);
                // Check the compatibility of arguments.
                boolean match = true;

                for (int j = 0; j < arguments.length && match; j++) {
                    ArgumentConversion conversion = _getConversion(
                            arguments[j], argumentTypes[j]);

                    // System.out.println("formalType is "
                    //        + arguments[j] + " " + arguments[j].getName());
                    // System.out.println("actualType is " + argumentTypes[j]
                    //        + " " + argumentTypes[j].getClass().getName());
                    match = match && conversion != IMPOSSIBLE_CONVERSION;
                    // System.out.println("match: " + match + " conversion: " + conversion);

                    conversions[j] = conversion;
                }

                // If there was a previous match, then check to see
                // which one is preferable.
                if (match && matchedMethod != null) {
                    // Set match to false if previously found match is
                    // preferable to this one.  matchedConversions is
                    // the set of previously found conversions.
                    match = _areConversionsPreferable(conversions, arguments,
                            matchedConversions,
                            matchedMethod.getParameterTypes());
                }

                if (match) {
                    // System.out.println("still a match after _areConversionsPreferable");
                    // If still a match, then remember the method for later,
                    // so it can be checked against any other match found.
                    matchedMethod = methods[i];
                    System.arraycopy(conversions, 0, matchedConversions, 0,
                            actualArgCount);
                }
            }

            library = library.getSuperclass();
        }

        System.arraycopy(matchedConversions, 0, conversions, 0,
                conversions.length);
        return matchedMethod;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Add the specified instance of this class to the cache.
     *  @param cachedMethod The instance to add to the cache.
     */
    private static void _addCachedMethod(CachedMethod cachedMethod) {
        _cachedMethods.put(cachedMethod, cachedMethod);
    }

    // Find a CachedMethod of type FUNCTION, in a registered class,
    // that accepts arguments argumentTypes[0..length].  Return null if no
    // method can be found.
    private static CachedMethod _findFUNCTION(String methodName,
            Type[] argumentTypes) throws IllegalActionException {
        CachedMethod cachedMethod = null;
        ArgumentConversion[] conversions = new ArgumentConversion[argumentTypes.length];

        // Search the registered function classes
        Iterator allClasses = PtParser.getRegisteredClasses().iterator();

        // Keep track of multiple matches, to try to find the
        // most specific one.
        Method preferredMethod = null;
        ArgumentConversion[] preferredConversions = null;

        while (allClasses.hasNext() && cachedMethod == null) {
            Class nextClass = (Class) allClasses.next();

            //System.out.println("Examining registered class: "
            //  + nextClass);
            try {
                Method method = _polymorphicGetMethod(nextClass, methodName,
                        argumentTypes, conversions);

                if (method != null) {
                    // System.out.println("Found match: " + method);
                    // Compare to previous match, if there has
                    // been one.
                    if (preferredMethod == null
                            || _areConversionsPreferable(conversions,
                                    method.getParameterTypes(),
                                    preferredConversions,
                                    preferredMethod.getParameterTypes())) {
                        // Either there is no previous match,
                        // or the current match is preferable
                        // or equivalent to the previous match.
                        preferredMethod = method;
                        // Kepler (jdk1.4?) requires this cast
                        preferredConversions = conversions.clone();
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
            cachedMethod = new CachedMethod(methodName, argumentTypes,
                    preferredMethod, preferredConversions, FUNCTION);
        }

        return cachedMethod;
    }

    // Find a CachedMethod of type METHOD, in a class that extends
    // from the type indicated by argumentTypes[0], that accepts arguments
    // argumentTypes[1..length].  Return null if no method can be found.
    private static CachedMethod _findMETHOD(String methodName,
            Type[] argumentTypes) throws IllegalActionException {
        CachedMethod cachedMethod = null;

        // Try to reflect the method.
        int num = argumentTypes.length;
        ArgumentConversion[] conversions = new ArgumentConversion[num - 1];

        Class destTokenClass = argumentTypes[0].getTokenClass();
        Type[] methodArgTypes;

        if (num == 1) {
            methodArgTypes = null;
        } else {
            methodArgTypes = new Type[num - 1];

            for (int i = 1; i < num; i++) {
                methodArgTypes[i - 1] = argumentTypes[i];
            }
        }

        try {
            Method method = _polymorphicGetMethod(destTokenClass, methodName,
                    methodArgTypes, conversions);

            if (method != null) {
                cachedMethod = new CachedMethod(methodName, argumentTypes,
                        method, conversions, METHOD);
            }
        } catch (SecurityException security) {
            // If we are running under an Applet, then we
            // may end up here if, for example, we try
            // to invoke the non-existent quantize function on
            // java.lang.Math.
        }

        if (cachedMethod == null) {
            // Native convert the base class.
            // System.out.println("Checking for base conversion");
            destTokenClass = ConversionUtilities
                    .convertTokenTypeToJavaType(argumentTypes[0]);

            Method method = _polymorphicGetMethod(destTokenClass, methodName,
                    methodArgTypes, conversions);

            if (method != null) {
                cachedMethod = new BaseConvertCachedMethod(methodName,
                        argumentTypes, method, NATIVE_CONVERSION, conversions);
            }
        }

        return cachedMethod;
    }

    /** Return the CachedMethod that corresponds to methodName and
     *  argumentTypes if it had been cached previously.
     */
    private static CachedMethod _getCachedMethod(String methodName,
            Type[] argumentTypes, int type) throws IllegalActionException {
        CachedMethod key = new CachedMethod(methodName, argumentTypes, null,
                null, type);

        // System.out.println("findMethod:" + key);
        CachedMethod method = _cachedMethods.get(key);
        return method;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // The method name.
    private String _methodName;

    // The token types of the arguments.
    private Type[] _argumentTypes;

    // The Java method to be invoked.
    private Method _method;

    // Conversions that convert the types of the arguments to types
    // acceptable by the given method.
    private ArgumentConversion[] _conversions;

    // The precomputed hashcode for this cached method.
    private int _hashcode;

    // The return type of the the method, as determined from the method itself,
    // or from a monotonic function.
    private Type _returnType;

    // The type.
    private int _type;

    // The static table containing cached methods.  Note that a
    // synchronized hashtable is used to provide safe access to the
    // table of methods from multiple threads.
    private static Hashtable<CachedMethod, CachedMethod> _cachedMethods = new Hashtable<CachedMethod, CachedMethod>();

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////
    ///////////////////////////////////////////////////////////////////
    //// ArgumentConversion

    /** Class representing an argument conversion.  Instances of this
     *  class are returned by getConversions().  Note that in most
     *  cases, it is not necessary to reference this class directly,
     *  as the invoke() method applies all the necessary conversions.
     *  It is provided for code, such as the code generator that need
     *  more than the usual amount of information about methods that
     *  have been found.
     *  <p>The preference is n index given an order to the preference of
     *  conversions.  Lower preferences represent less desirable
     *  conversions than higher preferences.
     */
    public static class ArgumentConversion {
        /** Construct an argument conversion.
         *  @param preference The preference of this conversion.
         *  The preference is n index given an order to the preference of
         *  conversions.  Lower preferences represent less desirable
         *  conversions than higher preferences.
         */
        private ArgumentConversion(int preference) {
            _preference = preference;
        }

        /** Return the preference of this conversion, relative to
         *  other conversions.  The higher the preference, the more
         *  preferable the conversion.
         *  @return The preference of this conversion.
         */
        public int getPreference() {
            return _preference;
        }

        /** Convert the given token into an object that can be used to
         *  invoke a method through the reflection mechanism.  Derived
         *  classes will override this method to provide different
         *  types of argument conversions.
         *  @param input The token to be converted
         *  @return The object that can be used to invoke a method
         *  through the reflection method.
         *  @exception IllegalActionException Always thrown in this
         *  base class.
         */
        public Object convert(ptolemy.data.Token input)
                throws IllegalActionException {
            throw new IllegalActionException("Cannot convert argument token "
                    + input);
        }

        /** Return true if this conversion is preferable to the given
         *  conversion.
         *  @param conversion The conversion to be tested.
         *  @return True if this conversion is prefereable to the given
         *  conversion.
         */
        public boolean isPreferableTo(ArgumentConversion conversion) {
            return _preference > conversion.getPreference();
        }

        /** Return a string representation of this conversion.
         *  @return A string representation of this conversion.
         */
        @Override
        public String toString() {
            return "Conversion " + _preference;
        }

        /**  The preference is n index given an order to the
         *  preference of conversions.  Lower preferences represent
         *  less desirable conversions than higher preferences.
         */
        protected int _preference;
    }

    ///////////////////////////////////////////////////////////////////
    //// TypeArgumentConversion

    /** A class representing an argument conversion to another ptolemy type,
     *  followed by the given conversion.
     *  This conversion always has preference two.
     */
    public static class TypeArgumentConversion extends ArgumentConversion {
        private TypeArgumentConversion(Type type, ArgumentConversion conversion) {
            super(2);
            _conversionType = type;
            _conversion = conversion;
        }

        /** Convert the given token into an object that can be used to
         *  invoke a method through the reflection mechanism.  Derived
         *  classes will override this method to provide different
         *  types of argument conversions.
         */
        @Override
        public Object convert(ptolemy.data.Token input)
                throws IllegalActionException {
            ptolemy.data.Token token = _conversionType.convert(input);
            return _conversion.convert(token);
        }

        /** Return true if this conversion is preferable to the given
         * conversion.
         */
        @Override
        public boolean isPreferableTo(ArgumentConversion conversion) {
            if (_preference > conversion.getPreference()) {
                return true;
            } else if (_preference == conversion.getPreference()) {
                // Assume it is a TypeArgumentConversion.
                TypeArgumentConversion argumentConversion = (TypeArgumentConversion) conversion;

                // FIXME: compare types.
                // System.out.println("types: " + _conversionType + " " + argumentConversion._conversionType);
                if (TypeLattice.compare(_conversionType,
                        argumentConversion._conversionType) == ptolemy.graph.CPO.LOWER) {
                    return true;
                }

                if (_conversionType == BaseType.INT
                        && argumentConversion._conversionType == BaseType.FLOAT) {
                    // If we evaluate abs(-1ub), we should get 1, not
                    // 1.0.  Return true a conversion to int is
                    // compared to a conversion to float, meaning a
                    // conversion to int is preferable to a conversion
                    // to float.

                    return true;
                }
                return _conversion
                        .isPreferableTo(argumentConversion._conversion);
            } else {
                return false;
            }
        }

        /** Return a string representation of this conversion.
         */
        @Override
        public String toString() {
            return "TypeConversion(" + _conversionType + ", " + _conversion
                    + ") " + _preference;
        }

        private ptolemy.data.type.Type _conversionType;

        private ArgumentConversion _conversion;
    }

    ///////////////////////////////////////////////////////////////////
    //// BaseConvertCachedMethod

    /** A cached method that converts the object on which the method
     *  is invoked as well as the arguments.  This allows us to, for
     *  example, invoke instance methods of ptolemy.math.Complex on
     *  tokens of type ComplexToken.  This cached method can only
     *  operate on methods.
     */
    public static class BaseConvertCachedMethod extends CachedMethod {
        private BaseConvertCachedMethod(String methodName,
                Type[] argumentTypes, Method method,
                ArgumentConversion baseConversion,
                ArgumentConversion[] conversions) throws IllegalActionException {
            super(methodName, argumentTypes, method, conversions, METHOD);
            _baseConversion = baseConversion;
        }

        /** Return the conversion that is applied to the object
         *  upon which the method is invoked.
         *  @return The conversion that is applied to the object
         *  upon which the method is invoked.
         */
        public ArgumentConversion getBaseConversion() {
            return _baseConversion;
        }

        @Override
        public ptolemy.data.Token invoke(Object[] argValues)
                throws IllegalActionException {
            argValues[0] = _baseConversion
                    .convert((ptolemy.data.Token) argValues[0]);
            return super.invoke(argValues);
        }

        private ArgumentConversion _baseConversion;
    }

    ///////////////////////////////////////////////////////////////////
    //// ArrayMapCachedMethod

    /** A class representing the invocation of a scalar method on
     *  an array of elements.
     */
    public static class ArrayMapCachedMethod extends CachedMethod {
        /**
         * Constructs a CachedMethod$ArrayMapCachedMethod object.
         *
         * @param methodName The name of the method.
         * @param argumentTypes The types of the arguments.
         * @param type An integer specifying the type
         * @param cachedMethod The method to be invoked
         * @param reducedArgs    An array of booleans where if an
         * element of the array is true and the corresponding argument
         * is an ArrayToken, then invoke() handles those arguments
         * specially.
         * @exception IllegalActionException Not thrown in this derived
         * class, but the superclass throws it if the return type of
         * the cached method cannot be determined.
         */
        public ArrayMapCachedMethod(String methodName, Type[] argumentTypes,
                int type, CachedMethod cachedMethod, boolean[] reducedArgs)
                        throws IllegalActionException {
            super(methodName, argumentTypes, null, null, type);
            _cachedMethod = cachedMethod;
            _reducedArgs = reducedArgs;
        }

        /** Invoke the method represented by this CachedMethod.  This
         *  implements any conversions necessary to turn token arguments
         *  into other arguments, and to convert the result back into
         *  a token.
         *  @param argValues An array of token objects that will be used
         *   as the arguments.  Note that each element must be an
         *   ArrayToken and each ArrayToken must have the same length
         *   as the other ArrayTokens.
         *  @return The token result of the method invocation.
         *  @exception IllegalActionException If the invoked method
         *   throws it.
         */
        @Override
        public ptolemy.data.Token invoke(Object[] argValues)
                throws IllegalActionException {
            int dim = 0;

            // Check the argument lengths.
            for (int i = 0; i < argValues.length; i++) {
                if (_reducedArgs[i]) {
                    if (argValues[i] instanceof ArrayToken) {
                        ArrayToken arrayToken = (ArrayToken) argValues[i];

                        if (dim != 0 && arrayToken.length() != dim) {
                            throw new IllegalActionException("Argument " + i
                                    + " is a reducible arrayToken that "
                                    + "does not have compatible length!");
                        } else {
                            dim = arrayToken.length();
                        }
                    } else {
                        throw new IllegalActionException("Argument " + i
                                + " is not an instance of " + "ArrayToken!");
                    }
                }
            }

            // Collect the not reducible args.
            // Kepler (jdk1.4?) requires this cast
            Object[] subArgs = argValues.clone();
            ptolemy.data.Token[] tokenArray = new ptolemy.data.Token[dim];

            for (int j = 0; j < dim; j++) {
                for (int i = 0; i < argValues.length; i++) {
                    if (_reducedArgs[i]) {
                        subArgs[i] = ((ArrayToken) argValues[i]).getElement(j);
                    }
                }

                tokenArray[j] = _cachedMethod.invoke(subArgs);
            }

            return new ArrayToken(tokenArray);
        }

        /** Override the base class to correctly implement the
         * isValid() method.
         */
        @Override
        public boolean isValid() {
            return _cachedMethod.isValid();
        }

        /** Override the base class to return an array type with the
         *  element type being the return type of the underlying scalar
         *  method.
         *  @return An ArrayType with an appropriate element type.
         */
        @Override
        public Type getReturnType() throws IllegalActionException {
            if (!isValid()) {
                throw new IllegalActionException(
                        "The return type of the method " + toString()
                        + " cannot be determined because "
                        + "no matching method was found.");
            }

            Type elementType = _cachedMethod.getReturnType();
            return new ArrayType(elementType);
        }

        /** Return an appropriate description of the method being invoked.
         *  @return A description of the method being invoked.
         */
        @Override
        public String methodDescription() {
            return "ArrayMapped{" + _cachedMethod.methodDescription() + "}";
        }

        private CachedMethod _cachedMethod;

        private boolean[] _reducedArgs;
    }

    ///////////////////////////////////////////////////////////////////
    //// MatrixMapCachedMethod

    /** A class representing the invocation of a scalar method on
     *  a matrix of elements.
     */
    public static class MatrixMapCachedMethod extends CachedMethod {
        /**
         * Constructs a CachedMethod$MatrixMapCachedMethod object.
         *
         * @param methodName The name of the method.
         * @param argumentTypes The types of the arguments.
         * @param type An integer specifying the type
         * @param cachedMethod The method to be invoked
         * @param reducedArgs    An array of booleans where if an
         * element of the array is true and the corresponding argument
         * is an MatrixToken, then invoke() handles those arguments
         * specially.
         * @exception IllegalActionException Not thrown in this derived
         * class, but the superclass throws it if the return type of
         * the cached method cannot be determined.
         */
        public MatrixMapCachedMethod(String methodName, Type[] argumentTypes,
                int type, CachedMethod cachedMethod, boolean[] reducedArgs)
                        throws IllegalActionException {
            super(methodName, argumentTypes, null, null, type);
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
        @Override
        public ptolemy.data.Token invoke(Object[] argValues)
                throws IllegalActionException {
            int xdim = 0;
            int ydim = 0;

            // Check the argument lengths.
            for (int i = 0; i < argValues.length; i++) {
                if (_reducedArgs[i]) {
                    if (argValues[i] instanceof MatrixToken) {
                        MatrixToken matrixToken = (MatrixToken) argValues[i];

                        if (xdim != 0
                                && ydim != 0
                                && (matrixToken.getRowCount() != ydim || matrixToken
                                .getColumnCount() != xdim)) {
                            throw new IllegalActionException("Argument " + i
                                    + " is a reducible matrixToken that "
                                    + "does not have compatible size!");
                        } else {
                            ydim = matrixToken.getRowCount();
                            xdim = matrixToken.getColumnCount();
                        }
                    } else {
                        throw new IllegalActionException("Argument " + i
                                + " is not an instance of " + "MatrixToken!");
                    }
                }
            }

            // Collect the not reducible args.
            // Kepler (jdk1.4?) requires this cast
            Object[] subArgs = argValues.clone();
            ptolemy.data.Token[] tokenArray = new ptolemy.data.Token[xdim
                                                                     * ydim];

            int pos = 0;

            for (int j = 0; j < ydim; j++) {
                for (int k = 0; k < xdim; k++) {
                    for (int i = 0; i < argValues.length; i++) {
                        if (_reducedArgs[i]) {
                            subArgs[i] = ((MatrixToken) argValues[i])
                                    .getElementAsToken(j, k);
                        }
                    }

                    tokenArray[pos++] = _cachedMethod.invoke(subArgs);
                }
            }

            return MatrixToken.arrayToMatrix(tokenArray, ydim, xdim);
        }

        /** Override the base class to correctly implement the
         * isValid() method.
         */
        @Override
        public boolean isValid() {
            return _cachedMethod.isValid();
        }

        @Override
        public Type getReturnType() throws IllegalActionException {
            if (!isValid()) {
                throw new IllegalActionException(
                        "The return type of the method " + toString()
                        + " cannot be determined because "
                        + "no matching method was found.");
            }

            Type elementType = _cachedMethod.getReturnType();
            return MatrixType.getMatrixTypeForElementType(elementType);
        }

        /** Return an appropriate description of the method being invoked.
         *  @return A description of the method being invoked.
         */
        @Override
        public String methodDescription() {
            return "MatrixMapped{" + _cachedMethod.methodDescription() + "}";
        }

        private CachedMethod _cachedMethod;

        private boolean[] _reducedArgs;
    }
}
